package com.zjnu.studybuddy.dto;

import lombok.Data;

@Data
public class LogQueryDTO {

    private String module;

    private String username;

    private String action;

    private Integer success;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    public Integer getOffset() {
        return (Math.max(pageNum, 1) - 1) * pageSize;
    }
}
