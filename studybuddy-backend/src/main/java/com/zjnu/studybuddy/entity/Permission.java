package com.zjnu.studybuddy.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Permission implements Serializable {

    private Long id;

    private String permCode;

    private String permName;

    private String resourceType;

    private String description;
}
