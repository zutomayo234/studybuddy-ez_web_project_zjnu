package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SessionVO implements Serializable {

    private Long id;
    private String title;

    private Long messageCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
