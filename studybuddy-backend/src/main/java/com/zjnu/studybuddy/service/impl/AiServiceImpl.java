package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.ai.ChatContextAssembler;
import com.zjnu.studybuddy.ai.LlmClient;
import com.zjnu.studybuddy.ai.PromptInjectionGuard;
import com.zjnu.studybuddy.ai.PromptTemplates;
import com.zjnu.studybuddy.ai.RagService;
import com.zjnu.studybuddy.common.BusinessException;
import com.zjnu.studybuddy.common.ResultCode;
import com.zjnu.studybuddy.dto.ChatDTO;
import com.zjnu.studybuddy.entity.ChatMessage;
import com.zjnu.studybuddy.service.AiService;
import com.zjnu.studybuddy.service.ChatSessionService;
import com.zjnu.studybuddy.vo.ChatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final int DEFAULT_CONTEXT_LIMIT = 8;

    private final PromptInjectionGuard guard;
    private final RagService ragService;
    private final LlmClient llmClient;
    private final ChatSessionService chatSessionService;
    private final com.zjnu.studybuddy.config.AiProperties aiProperties;

    private int contextLimit() {
        int limit = aiProperties.getHistoryLimit();
        return limit > 0 ? limit : DEFAULT_CONTEXT_LIMIT;
    }

    @Override
    public ChatVO chat(ChatDTO dto, Long userId) {

        String question = guard.sanitize(dto.getQuestion());

        if (guard.isTooLong(question)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "问题过长，请精简后再试（上限 2000 字）");
        }
        if (guard.isSuspicious(question)) {
            throw new BusinessException(ResultCode.PROMPT_INJECTION,
                    "检测到疑似提示注入内容，已拦截。请正常提问学习相关问题。");
        }

        Long sessionId = resolveSession(dto.getSessionId(), userId, question);

        List<ChatMessage> history = chatSessionService.recentContext(sessionId, contextLimit());

        List<String> references = Boolean.FALSE.equals(dto.getUseRag())
                ? List.of()
                : ragService.retrieve(question);

        chatSessionService.appendMessage(sessionId, ChatContextAssembler.ROLE_USER, question, null, false);

        ChatVO vo = new ChatVO();
        vo.setSessionId(sessionId);
        vo.setReferences(references);
        vo.setModel(llmClient.getModel());
        vo.setContextSize(history.size());

        String userPrompt = references.isEmpty()
                ? PromptTemplates.userPrompt(question)
                : PromptTemplates.userPromptWithContext(String.join("\n", references), question);

        String answer;

        if (!llmClient.isConfigured()) {
            vo.setFallback(true);
            answer = buildFallbackAnswer(question, references);
        } else {
            try {
                List<Map<String, String>> messages = ChatContextAssembler.toMessages(
                        PromptTemplates.SYSTEM_PROMPT, history, userPrompt, contextLimit());
                answer = llmClient.chat(messages);
            } catch (Exception e) {
                log.error("调用大模型失败，降级为本地检索回答", e);
                vo.setFallback(true);
                answer = buildFallbackAnswer(question, references)
                        + "\n\n（模型服务暂时不可用，以上为知识库直答结果。）";
            }
        }
        vo.setAnswer(answer);

        chatSessionService.appendMessage(sessionId, ChatContextAssembler.ROLE_ASSISTANT,
                answer, references, Boolean.TRUE.equals(vo.getFallback()));
        return vo;
    }

    private Long resolveSession(String sessionId, Long userId, String question) {
        if (sessionId == null || sessionId.isBlank()) {
            return chatSessionService.createSession(userId, question);
        }
        Long id;
        try {
            id = Long.valueOf(sessionId.trim());
        } catch (NumberFormatException e) {

            log.warn("sessionId 非数字，按新建会话处理: {}", sessionId);
            return chatSessionService.createSession(userId, question);
        }
        if (!chatSessionService.owns(userId, id)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该会话不存在或不属于当前用户");
        }
        return id;
    }

    private String buildFallbackAnswer(String question, List<String> references) {
        if (references.isEmpty()) {
            return "未配置大模型 API Key，且知识库中没有检索到与「" + question + "」相关的内容。\n"
                    + "请在环境变量 LLM_API_KEY 中配置密钥后重试。";
        }
        return "未配置大模型 API Key，以下为知识库中与该问题最相关的内容：\n\n"
                + String.join("\n\n", references);
    }
}
