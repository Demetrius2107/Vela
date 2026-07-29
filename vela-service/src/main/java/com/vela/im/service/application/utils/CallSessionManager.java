package com.vela.im.service.application.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 通话会话管理器，跟踪音视频通话的状态。
 */
@Component
public class CallSessionManager {

    private static final Logger log = LoggerFactory.getLogger(CallSessionManager.class);

    public enum CallStatus { RINGING, CONNECTED, ENDED }

    public static class CallSession {
        private final String callId;
        private final String callerId;
        private final String calleeId;
        private final boolean videoCall;
        private volatile CallStatus status;
        private final long startTime;

        public CallSession(String callId, String callerId, String calleeId, boolean videoCall) {
            this.callId = callId;
            this.callerId = callerId;
            this.calleeId = calleeId;
            this.videoCall = videoCall;
            this.status = CallStatus.RINGING;
            this.startTime = System.currentTimeMillis();
        }

        public String getCallId() { return callId; }
        public String getCallerId() { return callerId; }
        public String getCalleeId() { return calleeId; }
        public boolean isVideoCall() { return videoCall; }
        public CallStatus getStatus() { return status; }
        public void setStatus(CallStatus status) { this.status = status; }
        public long getStartTime() { return startTime; }
    }

    /** callId → CallSession */
    private final ConcurrentHashMap<String, CallSession> sessions = new ConcurrentHashMap<>();

    /** userId → current callId (each user can only be in one call) */
    private final ConcurrentHashMap<String, String> userCalls = new ConcurrentHashMap<>();

    public CallSession createCall(String callId, String callerId, String calleeId, boolean videoCall) {
        CallSession session = new CallSession(callId, callerId, calleeId, videoCall);
        sessions.put(callId, session);
        userCalls.put(callerId, callId);
        userCalls.put(calleeId, callId);
        log.info("Call created: {} ({} → {}, video={})", callId, callerId, calleeId, videoCall);
        return session;
    }

    public CallSession getCall(String callId) {
        return sessions.get(callId);
    }

    public boolean isUserInCall(String userId) {
        return userCalls.containsKey(userId);
    }

    public String getUserCurrentCallId(String userId) {
        return userCalls.get(userId);
    }

    public void endCall(String callId) {
        CallSession session = sessions.remove(callId);
        if (session != null) {
            userCalls.remove(session.getCallerId());
            userCalls.remove(session.getCalleeId());
            log.info("Call ended: {}", callId);
        }
    }
}
