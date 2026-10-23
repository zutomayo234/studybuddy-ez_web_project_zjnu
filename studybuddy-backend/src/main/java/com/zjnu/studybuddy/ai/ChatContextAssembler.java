package com.zjnu.studybuddy.ai;

import com.zjnu.studybuddy.entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ChatContextAssembler {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    private ChatContextAssembler() {
    }

    public static List<Map<String, String>> toMessages(String systemPrompt,
                                                       List<ChatMessage> history,
                                                       String userPrompt,
                                                       int maxHistory) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", ROLE_SYSTEM, "content", nullSafe(systemPrompt)));

        for (ChatMessage m : tail(history, maxHistory)) {
            if (m == null || !isValidRole(m.getRole()) || m.getContent() == null || m.getContent().isBlank()) {
                continue;
            }
            messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }

        messages.add(Map.of("role", ROLE_USER, "content", nullSafe(userPrompt)));
        return messages;
    }

    private static List<ChatMessage> tail(List<ChatMessage> history, int maxHistory) {
        if (history == null || history.isEmpty() || maxHistory <= 0) {
            return List.of();
        }
        int from = Math.max(0, history.size() - maxHistory);
        return history.subList(from, history.size());
    }

    private static boolean isValidRole(String role) {
        return ROLE_USER.equals(role) || ROLE_ASSISTANT.equals(role);
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
