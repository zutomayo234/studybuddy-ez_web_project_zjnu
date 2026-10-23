package com.zjnu.studybuddy.mapper;

import com.zjnu.studybuddy.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage message);

    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    List<ChatMessage> selectRecentBySession(@Param("sessionId") Long sessionId,
                                            @Param("limit") int limit);

    long countBySessionId(@Param("sessionId") Long sessionId);

    int deleteBySessionId(@Param("sessionId") Long sessionId);

    long countAll();
}
