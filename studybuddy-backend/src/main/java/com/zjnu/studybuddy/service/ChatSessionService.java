package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.entity.ChatMessage;
import com.zjnu.studybuddy.vo.MessageVO;
import com.zjnu.studybuddy.vo.SessionVO;

import java.util.List;

public interface ChatSessionService {

    Long createSession(Long userId, String firstQuestion);

    List<SessionVO> listSessions(Long userId);

    List<MessageVO> listMessages(Long userId, Long sessionId);

    boolean owns(Long userId, Long sessionId);

    void deleteSession(Long userId, Long sessionId);

    void appendMessage(Long sessionId, String role, String content,
                       List<String> references, boolean fallback);

    List<ChatMessage> recentContext(Long sessionId, int limit);
}
