import { ref } from 'vue'
import { send, on } from './socket'

const CALL_COMMANDS = {
  OFFER: 0x900,
  ANSWER: 0x901,
  ICE: 0x902,
  HANGUP: 0x903,
  REJECT: 0x904,
  BUSY: 0x905,
}

const peerConnection = ref(null)
const localStream = ref(null)
const remoteStream = ref(null)
const callStatus = ref('idle') // idle | ringing | calling | connected
const remoteUserId = ref('')
const isVideoCall = ref(false)

const iceServers = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] }

export function useWebRTC() {
  function initPeerConnection() {
    const pc = new RTCPeerConnection(iceServers)
    pc.onicecandidate = (e) => {
      if (e.candidate) {
        send({ command: CALL_COMMANDS.ICE, callId: getCallId(), iceCandidate: e.candidate })
      }
    }
    pc.ontrack = (e) => { remoteStream.value = e.streams[0] }
    return pc
  }

  function getCallId() {
    // Simple call ID from caller + timestamp
    return `${remoteUserId.value}_${Date.now()}`
  }

  async function startCall(userId, video) {
    remoteUserId.value = userId
    isVideoCall.value = video
    callStatus.value = 'calling'
    const pc = initPeerConnection()
    peerConnection.value = pc
    const stream = await navigator.mediaDevices.getUserMedia({ video, audio: true })
    localStream.value = stream
    stream.getTracks().forEach(t => pc.addTrack(t, stream))
    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)
    send({ command: CALL_COMMANDS.OFFER, callId: getCallId(), targetId: userId, sdp: offer, video })
  }

  async function acceptCall(data) {
    callStatus.value = 'connected'
    remoteUserId.value = data.fromId
    isVideoCall.value = !!data.video
    const pc = initPeerConnection()
    peerConnection.value = pc
    const stream = await navigator.mediaDevices.getUserMedia({ video: data.video, audio: true })
    localStream.value = stream
    stream.getTracks().forEach(t => pc.addTrack(t, stream))
    await pc.setRemoteDescription(new RTCSessionDescription(data.sdp))
    const answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)
    send({ command: CALL_COMMANDS.ANSWER, callId: data.callId, sdp: answer })
  }

  function handleRemoteAnswer(data) {
    if (peerConnection.value) {
      peerConnection.value.setRemoteDescription(new RTCSessionDescription(data.sdp))
      callStatus.value = 'connected'
    }
  }

  function handleIce(data) {
    if (peerConnection.value && data.iceCandidate) {
      peerConnection.value.addIceCandidate(new RTCIceCandidate(data.iceCandidate))
    }
  }

  function endCall() {
    if (peerConnection.value) {
      peerConnection.value.close()
      peerConnection.value = null
    }
    if (localStream.value) {
      localStream.value.getTracks().forEach(t => t.stop())
      localStream.value = null
    }
    remoteStream.value = null
    callStatus.value = 'idle'
    send({ command: CALL_COMMANDS.HANGUP, callId: getCallId(), targetId: remoteUserId.value })
  }

  function rejectCall(data) {
    send({ command: CALL_COMMANDS.REJECT, callId: data.callId, targetId: data.fromId })
    callStatus.value = 'idle'
  }

  // Register signaling handlers
  function registerHandlers() {
    on(CALL_COMMANDS.OFFER, (data) => {
      remoteUserId.value = data.fromId
      isVideoCall.value = !!data.video
      callStatus.value = 'ringing'
      // Store offer data for accept
      window.__pendingCall = data
    })
    on(CALL_COMMANDS.ANSWER, handleRemoteAnswer)
    on(CALL_COMMANDS.ICE, handleIce)
    on(CALL_COMMANDS.HANGUP, () => { callStatus.value = 'idle'; endCall() })
    on(CALL_COMMANDS.REJECT, () => { callStatus.value = 'idle' })
    on(CALL_COMMANDS.BUSY, () => { callStatus.value = 'idle'; alert('对方正忙') })
  }

  return {
    peerConnection, localStream, remoteStream, callStatus, remoteUserId, isVideoCall,
    startCall, acceptCall, rejectCall, endCall, registerHandlers,
  }
}
