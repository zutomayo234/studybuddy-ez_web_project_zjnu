package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PermissionVO implements Serializable {

    private Long id;
    private String permCode;
    private String permName;
    private String resourceType;
    private String description;
}
