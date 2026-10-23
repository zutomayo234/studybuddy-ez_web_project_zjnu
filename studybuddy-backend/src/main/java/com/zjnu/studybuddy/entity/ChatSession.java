package com.zjnu.studybuddy.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ChatSession implements Serializable {

    private Long id;

    private Long userId;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
