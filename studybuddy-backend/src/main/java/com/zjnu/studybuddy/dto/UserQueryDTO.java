package com.zjnu.studybuddy.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String keyword;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    public Integer getOffset() {
        return (Math.max(pageNum, 1) - 1) * pageSize;
    }
}
