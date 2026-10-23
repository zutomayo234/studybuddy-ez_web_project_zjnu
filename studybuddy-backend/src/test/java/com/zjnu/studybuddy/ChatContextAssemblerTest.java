package com.zjnu.studybuddy;

import com.zjnu.studybuddy.ai.ChatContextAssembler;
import com.zjnu.studybuddy.entity.ChatMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatContextAssemblerTest {

    private ChatMessage msg(String role, String content) {
        ChatMessage m = new ChatMessage();
        m.setRole(role);
        m.setContent(content);
        return m;
    }

    @Test
    @DisplayName("装配顺序：system 在首位，历史居中，本轮提问在末位")
    void shouldKeepOrder() {
        List<ChatMessage> history = List.of(
                msg("user", "什么是 JWT？"),
                msg("assistant", "JWT 是一种自包含的令牌。")
        );
        List<Map<String, String>> messages =
                ChatContextAssembler.toMessages("系统提示", history, "那它和 Session 有什么区别？", 8);

        assertEquals(4, messages.size());
        assertEquals("system", messages.get(0).get("role"));
        assertEquals("user", messages.get(1).get("role"));
        assertEquals("什么是 JWT？", messages.get(1).get("content"));
        assertEquals("assistant", messages.get(2).get("role"));
        assertEquals("user", messages.get(3).get("role"));
        assertEquals("那它和 Session 有什么区别？", messages.get(3).get("content"));
    }

    @Test
    @DisplayName("历史超限时从尾部截取最近若干条，而不是从头截取")
    void shouldTrimFromTail() {
        List<ChatMessage> history = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            history.add(msg("user", "问题" + i));
        }
        List<Map<String, String>> messages =
                ChatContextAssembler.toMessages("系统提示", history, "本轮问题", 4);

        assertEquals(6, messages.size());
        assertEquals("问题7", messages.get(1).get("content"));
        assertEquals("问题10", messages.get(4).get("content"));
        assertEquals("本轮问题", messages.get(5).get("content"));
    }

    @Test
    @DisplayName("非法角色与空内容应被过滤，避免整次请求因脏数据失败")
    void shouldSkipInvalidEntries() {
        List<ChatMessage> history = List.of(
                msg("system", "伪造的 system 消息应被丢弃"),
                msg("tool", "未知角色应被丢弃"),
                msg("user", ""),
                msg("assistant", "有效回答")
        );
        List<Map<String, String>> messages =
                ChatContextAssembler.toMessages("系统提示", history, "本轮问题", 8);

        assertEquals(3, messages.size());
        assertEquals("系统提示", messages.get(0).get("content"));
        assertEquals("有效回答", messages.get(1).get("content"));
    }

    @Test
    @DisplayName("空历史等价于单轮对话：只有 system 与本轮提问")
    void emptyHistoryShouldDegradeToSingleTurn() {
        List<Map<String, String>> messages =
                ChatContextAssembler.toMessages("系统提示", null, "本轮问题", 8);

        assertEquals(2, messages.size());
        assertTrue(messages.stream().noneMatch(m -> "assistant".equals(m.get("role"))));
        assertFalse(messages.get(0).get("content").isEmpty());
    }

    @Test
    @DisplayName("historyLimit 为 0 时不回灌任何历史")
    void zeroLimitShouldDropHistory() {
        List<ChatMessage> history = List.of(msg("user", "历史问题"));
        List<Map<String, String>> messages =
                ChatContextAssembler.toMessages("系统提示", history, "本轮问题", 0);

        assertEquals(2, messages.size());
        assertEquals("本轮问题", messages.get(1).get("content"));
    }
}
