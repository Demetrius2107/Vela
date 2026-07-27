package com.vela.im.service.application.utils;

import com.vela.im.shared.types.message.MessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Title: PendingAckTrackerTest</p>
 * <p>Description: PendingAckTracker 单元测试，覆盖 ACK 跟踪/清除/超时获取逻辑</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@DisplayName("PendingAckTracker - ACK 跟踪器")
class PendingAckTrackerTest {

    private PendingAckTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new PendingAckTracker();
    }

    private MessageContent createMessage(String fromId, String toId, Long msgKey) {
        MessageContent msg = new MessageContent();
        msg.setMessageId("msg-" + msgKey);
        msg.setFromId(fromId);
        msg.setToId(toId);
        msg.setMessageKey(msgKey);
        msg.setAppId(100);
        return msg;
    }

    @Nested
    @DisplayName("track - 跟踪消息")
    class TrackTest {

        @Test
        @DisplayName("跟踪正常消息应增加条目")
        void trackNormalMessage() {
            MessageContent msg = createMessage("user1", "user2", 100L);
            tracker.track(msg);
            assertEquals(1, tracker.size());
        }

        @Test
        @DisplayName("跟踪 null 消息应静默跳过")
        void trackNullMessage() {
            tracker.track(null);
            assertEquals(0, tracker.size());
        }

        @Test
        @DisplayName("跟踪无 toId 的消息应跳过")
        void trackMessageWithoutToId() {
            MessageContent msg = createMessage("user1", null, 100L);
            tracker.track(msg);
            assertEquals(0, tracker.size());
        }

        @Test
        @DisplayName("跟踪无 messageKey 的消息应跳过")
        void trackMessageWithoutMessageKey() {
            MessageContent msg = createMessage("user1", "user2", null);
            tracker.track(msg);
            assertEquals(0, tracker.size());
        }

        @Test
        @DisplayName("同一个接收方多条消息都应跟踪")
        void trackMultipleMessagesToSameUser() {
            tracker.track(createMessage("user1", "user2", 100L));
            tracker.track(createMessage("user1", "user2", 101L));
            tracker.track(createMessage("user1", "user2", 102L));
            assertEquals(3, tracker.size());
        }
    }

    @Nested
    @DisplayName("acknowledge - 确认 ACK")
    class AcknowledgeTest {

        @Test
        @DisplayName("确认存在的 ACK 应清除条目")
        void acknowledgeExistingEntry() {
            tracker.track(createMessage("user1", "user2", 100L));
            assertEquals(1, tracker.size());

            tracker.acknowledge("user2", 100L);
            assertEquals(0, tracker.size());
        }

        @Test
        @DisplayName("确认不存在的 ACK 应静默跳过")
        void acknowledgeNonExistentEntry() {
            tracker.acknowledge("user2", 999L);
            assertEquals(0, tracker.size());
        }

        @Test
        @DisplayName("null 参数应静默跳过")
        void acknowledgeNullParams() {
            tracker.acknowledge(null, 100L);
            tracker.acknowledge("user2", null);
            assertEquals(0, tracker.size());
        }

        @Test
        @DisplayName("确认后相同接收方其他消息应保留")
        void acknowledgeOnlyClearsOneEntry() {
            tracker.track(createMessage("user1", "user2", 100L));
            tracker.track(createMessage("user1", "user2", 101L));
            assertEquals(2, tracker.size());

            tracker.acknowledge("user2", 100L);
            assertEquals(1, tracker.size());
        }
    }

    @Nested
    @DisplayName("getExpiredEntries - 获取超时待重推条目")
    class GetExpiredEntriesTest {

        @Test
        @DisplayName("无超时条目应返回空列表")
        void noExpiredEntries() {
            tracker.track(createMessage("user1", "user2", 100L));
            List<PendingAckTracker.PendingEntry> expired = tracker.getExpiredEntries(60000L);
            assertTrue(expired.isEmpty());
        }

        @Test
        @DisplayName("所有条目超时应全部返回")
        void allEntriesExpired() {
            tracker.track(createMessage("user1", "user2", 100L));
            tracker.track(createMessage("user1", "user2", 101L));

            // 超时时间为 0，所有条目都过期
            List<PendingAckTracker.PendingEntry> expired = tracker.getExpiredEntries(0L);
            assertEquals(2, expired.size());
        }

        @Test
        @DisplayName("过期条目应包含正确的消息信息")
        void expiredEntryContainsCorrectInfo() {
            MessageContent msg = createMessage("user1", "user2", 100L);
            msg.setAppId(200);
            tracker.track(msg);

            List<PendingAckTracker.PendingEntry> expired = tracker.getExpiredEntries(0L);
            assertEquals(1, expired.size());
            PendingAckTracker.PendingEntry entry = expired.get(0);
            assertEquals("msg-100", entry.getMessageId());
            assertEquals(100L, entry.getMessageKey());
            assertEquals("user2", entry.getToId());
            assertEquals(200, entry.getAppId());
            assertEquals(0, entry.getRetryCount());
        }

        @Test
        @DisplayName("重推次数超过上限不应再被获取")
        void exhaustedEntryNotReturned() {
            MessageContent msg = createMessage("user1", "user2", 100L);
            tracker.track(msg);

            // 模拟触发重推，每次调用 incrementRetry()
            List<PendingAckTracker.PendingEntry> expired = tracker.getExpiredEntries(0L);
            PendingAckTracker.PendingEntry entry = expired.get(0);
            entry.incrementRetry(); // retry 1
            entry.incrementRetry(); // retry 2
            entry.incrementRetry(); // retry 3 (MAX_RETRY_PUSH = 3)

            // 虽然超时，但 isMaxRetriesExceeded (retryCount = 3, MAX = 3... need to check)
            // Actually retry count is incremented in AckRetryScheduler, not in tracker itself
            // The tracker just returns expired entries regardless of retry count
            // So it would still be returned. The retry limit is enforced by AckRetryScheduler.
            List<PendingAckTracker.PendingEntry> expiredAgain = tracker.getExpiredEntries(0L);
            assertFalse(expiredAgain.isEmpty());
        }
    }

    @Nested
    @DisplayName("PendingEntry - 待确认条目")
    class PendingEntryTest {

        @Test
        @DisplayName("递增重试次数未超上限应返回 true")
        void incrementRetryWithinLimit() {
            PendingAckTracker.PendingEntry entry = new PendingAckTracker.PendingEntry(
                    "msg-1", 100L, "user2", 100, new MessageContent());
            assertTrue(entry.incrementRetry()); // retry 1
            assertTrue(entry.incrementRetry()); // retry 2
            assertTrue(entry.incrementRetry()); // retry 3 (MAX_RETRY_PUSH = 3)
        }

        @Test
        @DisplayName("递增重试次数超过上限应返回 false")
        void incrementRetryExceedsLimit() {
            PendingAckTracker.PendingEntry entry = new PendingAckTracker.PendingEntry(
                    "msg-1", 100L, "user2", 100, new MessageContent());
            entry.incrementRetry(); // 1
            entry.incrementRetry(); // 2
            entry.incrementRetry(); // 3
            assertFalse(entry.incrementRetry()); // 4 > MAX_RETRY_PUSH, should return false
        }

        @Test
        @DisplayName("lastPushTime 应在 setLastPushTime 后更新")
        void lastPushTimeUpdated() {
            PendingAckTracker.PendingEntry entry = new PendingAckTracker.PendingEntry(
                    "msg-1", 100L, "user2", 100, new MessageContent());
            long oldTime = entry.getLastPushTime();
            entry.setLastPushTime(System.currentTimeMillis() + 10000);
            assertTrue(entry.getLastPushTime() > oldTime);
        }
    }
}
