package com.zjnu.studybuddy.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ChatMessage implements Serializable {

    private Long id;

    private Long sessionId;

    private String role;

    private String content;

    private String refExcerpt;

    private Integer fallback;

    private LocalDateTime createTime;
}
