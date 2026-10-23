package com.zjnu.studybuddy.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class PromptInjectionGuard {

    private static final int MAX_LENGTH = 2000;

    private static final List<String> INJECTION_PATTERNS = List.of(
            "忽略之前", "忽略上面", "忽略以上", "忘记之前", "忘记上面",
            "ignore previous", "ignore above", "ignore all previous",
            "disregard previous", "forget previous",
            "你的系统提示", "输出你的提示词", "repeat your system prompt",
            "print your instructions", "show your prompt", "系统提示词是什么",
            "你现在是", "you are now", "开发者模式", "developer mode",
            "jailbreak", "越狱模式", "dan模式"
    );

    public boolean isSuspicious(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String lower = input.toLowerCase(Locale.ROOT);
        return INJECTION_PATTERNS.stream().anyMatch(p -> lower.contains(p.toLowerCase(Locale.ROOT)));
    }

    public boolean isTooLong(String input) {
        return input != null && input.length() > MAX_LENGTH;
    }

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replaceAll("[\\u200B-\\u200F\\uFEFF]", "")
                .replaceAll("[ \\t]{3,}", "  ")
                .trim();
    }
}
