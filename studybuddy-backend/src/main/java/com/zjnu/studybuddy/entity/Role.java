package com.zjnu.studybuddy.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Role implements Serializable {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<Permission> permissions;
}
