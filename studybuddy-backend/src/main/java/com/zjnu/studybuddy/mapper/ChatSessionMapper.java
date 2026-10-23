package com.zjnu.studybuddy.mapper;

import com.zjnu.studybuddy.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    int insert(ChatSession session);

    ChatSession selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    List<ChatSession> selectByUserId(@Param("userId") Long userId);

    int touch(@Param("id") Long id);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    long countByUserId(@Param("userId") Long userId);

    long countAll();
}
