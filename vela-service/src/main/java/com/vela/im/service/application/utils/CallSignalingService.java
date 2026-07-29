package com.vela.im.service.application.utils;

import com.vela.im.shared.types.ClientInfo;
import com.vela.im.shared.types.enums.command.MessageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * WebRTC 音视频通话信令服务，负责信令消息的转发和通话生命周期管理。
 */
@Service
public class CallSignalingService {

    private static final Logger log = LoggerFactory.getLogger(CallSignalingService.class);

    private final CallSessionManager callSessionManager;
    private final MessageProducer messageProducer;

    public CallSignalingService(CallSessionManager callSessionManager,
                                MessageProducer messageProducer) {
        this.callSessionManager = callSessionManager;
        this.messageProducer = messageProducer;
    }

    public void handleOffer(String callId, Integer appId, String callerId,
                            String calleeId, boolean videoCall, Object sdpOffer) {
        if (callSessionManager.isUserInCall(calleeId)) {
            messageProducer.sendToUser(callerId, MessageCommand.CALL_BUSY,
                    "callee is busy", appId);
            return;
        }
        callSessionManager.createCall(callId, callerId, calleeId, videoCall);
        messageProducer.sendToUser(calleeId, MessageCommand.CALL_OFFER, sdpOffer, appId);
        log.info("Call offer relayed: callId={}, caller={}, callee={}", callId, callerId, calleeId);
    }

    public void handleAnswer(String callId, Integer appId, String userId, Object sdpAnswer) {
        CallSessionManager.CallSession session = callSessionManager.getCall(callId);
        if (session == null) return;
        session.setStatus(CallSessionManager.CallStatus.CONNECTED);
        String target = userId.equals(session.getCallerId()) ? session.getCalleeId() : session.getCallerId();
        messageProducer.sendToUser(target, MessageCommand.CALL_ANSWER, sdpAnswer, appId);
    }

    public void handleIceCandidate(String callId, Integer appId, String fromUserId, Object iceCandidate) {
        CallSessionManager.CallSession session = callSessionManager.getCall(callId);
        if (session == null) return;
        String target = fromUserId.equals(session.getCallerId()) ? session.getCalleeId() : session.getCallerId();
        messageProducer.sendToUser(target, MessageCommand.CALL_ICE_CANDIDATE, iceCandidate, appId);
    }

    public void handleHangup(String callId, Integer appId, String userId) {
        CallSessionManager.CallSession session = callSessionManager.getCall(callId);
        if (session == null) return;
        session.setStatus(CallSessionManager.CallStatus.ENDED);
        String otherParty = userId.equals(session.getCallerId()) ? session.getCalleeId() : session.getCallerId();
        messageProducer.sendToUser(otherParty, MessageCommand.CALL_HANGUP, "call ended", appId);
        callSessionManager.endCall(callId);
    }

    public void handleReject(String callId, Integer appId, String userId) {
        CallSessionManager.CallSession session = callSessionManager.getCall(callId);
        if (session == null) return;
        messageProducer.sendToUser(session.getCallerId(), MessageCommand.CALL_REJECT,
                userId + " rejected the call", appId);
        callSessionManager.endCall(callId);
    }
}
