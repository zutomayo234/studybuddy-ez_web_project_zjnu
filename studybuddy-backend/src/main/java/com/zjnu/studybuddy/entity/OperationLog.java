package com.zjnu.studybuddy.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OperationLog implements Serializable {

    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String action;

    private String httpMethod;

    private String uri;

    private String ip;

    private Integer success;

    private Long costMs;

    private String detail;

    private LocalDateTime createTime;
}
