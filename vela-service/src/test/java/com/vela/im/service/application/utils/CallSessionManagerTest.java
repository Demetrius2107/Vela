package com.vela.im.service.application.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CallSessionManager - 通话会话管理")
class CallSessionManagerTest {

    private CallSessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new CallSessionManager();
    }

    @Test
    @DisplayName("创建通话应返回会话并标记双方为通话中")
    void createCallMarksBothUsers() {
        CallSessionManager.CallSession session = manager.createCall("call1", "userA", "userB", false);

        assertNotNull(session);
        assertEquals("call1", session.getCallId());
        assertTrue(manager.isUserInCall("userA"));
        assertTrue(manager.isUserInCall("userB"));
        assertEquals(CallSessionManager.CallStatus.RINGING, session.getStatus());
    }

    @Test
    @DisplayName("获取不存在的通话应返回 null")
    void getNonExistentCallReturnsNull() {
        assertNull(manager.getCall("nonexistent"));
    }

    @Test
    @DisplayName("结束通话应移除双方通话状态")
    void endCallRemovesBothUsers() {
        manager.createCall("call1", "userA", "userB", true);
        assertTrue(manager.isUserInCall("userA"));

        manager.endCall("call1");

        assertFalse(manager.isUserInCall("userA"));
        assertFalse(manager.isUserInCall("userB"));
        assertNull(manager.getCall("call1"));
    }

    @Test
    @DisplayName("获取用户当前通话ID")
    void getUserCurrentCallId() {
        manager.createCall("call1", "userA", "userB", false);

        assertEquals("call1", manager.getUserCurrentCallId("userA"));
        assertEquals("call1", manager.getUserCurrentCallId("userB"));
    }

    @Test
    @DisplayName("视频通话标记正确")
    void videoCallFlag() {
        CallSessionManager.CallSession session = manager.createCall("call1", "userA", "userB", true);

        assertTrue(session.isVideoCall());
    }

    @Test
    @DisplayName("更新通话状态")
    void updateCallStatus() {
        CallSessionManager.CallSession session = manager.createCall("call1", "userA", "userB", false);
        assertEquals(CallSessionManager.CallStatus.RINGING, session.getStatus());

        session.setStatus(CallSessionManager.CallStatus.CONNECTED);

        assertEquals(CallSessionManager.CallStatus.CONNECTED, session.getStatus());
    }
}
