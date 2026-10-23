package com.zjnu.studybuddy.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjnu.studybuddy.common.BusinessException;
import com.zjnu.studybuddy.common.ResultCode;
import com.zjnu.studybuddy.entity.ChatMessage;
import com.zjnu.studybuddy.entity.ChatSession;
import com.zjnu.studybuddy.mapper.ChatMessageMapper;
import com.zjnu.studybuddy.mapper.ChatSessionMapper;
import com.zjnu.studybuddy.service.ChatSessionService;
import com.zjnu.studybuddy.vo.MessageVO;
import com.zjnu.studybuddy.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final int TITLE_MAX = 30;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Long createSession(Long userId, String firstQuestion) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(buildTitle(firstQuestion));
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public List<SessionVO> listSessions(Long userId) {
        return sessionMapper.selectByUserId(userId).stream().map(s -> {
            SessionVO vo = new SessionVO();
            vo.setId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setCreateTime(s.getCreateTime());
            vo.setUpdateTime(s.getUpdateTime());
            vo.setMessageCount(messageMapper.countBySessionId(s.getId()));
            return vo;
        }).toList();
    }

    @Override
    public List<MessageVO> listMessages(Long userId, Long sessionId) {
        requireOwned(userId, sessionId);
        return messageMapper.selectBySessionId(sessionId).stream().map(this::toVO).toList();
    }

    @Override
    public boolean owns(Long userId, Long sessionId) {
        return userId != null && sessionId != null
                && sessionMapper.selectByIdAndUser(sessionId, userId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long userId, Long sessionId) {
        requireOwned(userId, sessionId);
        messageMapper.deleteBySessionId(sessionId);
        sessionMapper.deleteByIdAndUser(sessionId, userId);
    }

    @Override
    public void appendMessage(Long sessionId, String role, String content,
                              List<String> references, boolean fallback) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setRefExcerpt(writeReferences(references));
        msg.setFallback(fallback ? 1 : 0);
        messageMapper.insert(msg);
        sessionMapper.touch(sessionId);
    }

    @Override
    public List<ChatMessage> recentContext(Long sessionId, int limit) {
        if (sessionId == null || limit <= 0) {
            return List.of();
        }
        return messageMapper.selectRecentBySession(sessionId, limit);
    }

    private void requireOwned(Long userId, Long sessionId) {
        if (sessionId == null || sessionMapper.selectByIdAndUser(sessionId, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在或不属于当前用户");
        }
    }

    private String buildTitle(String question) {
        if (!StringUtils.hasText(question)) {
            return "新会话";
        }
        String t = question.replaceAll("\\s+", " ").trim();
        return t.length() <= TITLE_MAX ? t : t.substring(0, TITLE_MAX) + "…";
    }

    private String writeReferences(List<String> references) {
        if (references == null || references.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(references);

            return json.length() <= 1000 ? json : json.substring(0, 997) + "...";
        } catch (Exception e) {
            log.warn("序列化知识库出处失败，忽略：{}", e.getMessage());
            return null;
        }
    }

    private List<String> readReferences(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("反序列化知识库出处失败，按空处理：{}", e.getMessage());
            return List.of();
        }
    }

    private MessageVO toVO(ChatMessage m) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setRole(m.getRole());
        vo.setContent(m.getContent());
        vo.setReferences(readReferences(m.getRefExcerpt()));
        vo.setFallback(m.getFallback() != null && m.getFallback() == 1);
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }
}
