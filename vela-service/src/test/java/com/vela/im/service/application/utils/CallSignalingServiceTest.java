package com.vela.im.service.application.utils;

import com.vela.im.shared.types.message.MessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CallSignalingService - 通话信令服务")
class CallSignalingServiceTest {

    @Mock
    private CallSessionManager callSessionManager;
    @Mock
    private MessageProducer messageProducer;

    private CallSignalingService signalingService;

    @BeforeEach
    void setUp() {
        signalingService = new CallSignalingService(callSessionManager, messageProducer);
    }

    @Test
    @DisplayName("被叫空闲时应创建通话并转发 Offer")
    void calleeAvailableForwardsOffer() {
        when(callSessionManager.isUserInCall("userB")).thenReturn(false);

        signalingService.handleOffer("call1", 100, "userA", "userB", false, "sdp_offer");

        verify(callSessionManager, times(1)).createCall("call1", "userA", "userB", false);
        verify(messageProducer, times(1)).sendToUser(eq("userB"), any(), any(), eq(100));
    }

    @Test
    @DisplayName("被叫忙时应发送忙线通知")
    void calleeBusySendsBusy() {
        when(callSessionManager.isUserInCall("userB")).thenReturn(true);

        signalingService.handleOffer("call1", 100, "userA", "userB", true, "sdp_offer");

        verify(callSessionManager, never()).createCall(any(), any(), any(), anyBoolean());
        verify(messageProducer, times(1)).sendToUser(eq("userA"), any(), any(), eq(100));
    }

    @Test
    @DisplayName("处理 Answer 应转发给主叫")
    void answerForwardedToCaller() {
        CallSessionManager.CallSession session = mock(CallSessionManager.CallSession.class);
        when(session.getCallerId()).thenReturn("userA");
        when(session.getCalleeId()).thenReturn("userB");
        when(callSessionManager.getCall("call1")).thenReturn(session);

        signalingService.handleAnswer("call1", 100, "userB", "sdp_answer");

        verify(messageProducer, times(1)).sendToUser(eq("userA"), any(), any(), eq(100));
    }

    @Test
    @DisplayName("不存在的通话不应转发 Answer")
    void nonExistentCallSkipsAnswer() {
        when(callSessionManager.getCall("call_none")).thenReturn(null);

        signalingService.handleAnswer("call_none", 100, "userB", "sdp_answer");

        verify(messageProducer, never()).sendToUser(anyString(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("处理挂断应通知对方并结束通话")
    void hangupNotifiesOtherAndEndsCall() {
        CallSessionManager.CallSession session = mock(CallSessionManager.CallSession.class);
        when(session.getCallerId()).thenReturn("userA");
        when(session.getCalleeId()).thenReturn("userB");
        when(callSessionManager.getCall("call1")).thenReturn(session);

        signalingService.handleHangup("call1", 100, "userA");

        verify(messageProducer, times(1)).sendToUser(eq("userB"), any(), any(), eq(100));
        verify(callSessionManager, times(1)).endCall("call1");
    }

    @Test
    @DisplayName("拒绝通话应通知主叫并结束")
    void rejectNotifiesCallerAndEnds() {
        CallSessionManager.CallSession session = mock(CallSessionManager.CallSession.class);
        when(session.getCallerId()).thenReturn("userA");
        when(callSessionManager.getCall("call1")).thenReturn(session);

        signalingService.handleReject("call1", 100, "userB");

        verify(messageProducer, times(1)).sendToUser(eq("userA"), any(), any(), eq(100));
        verify(callSessionManager, times(1)).endCall("call1");
    }
}
