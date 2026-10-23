package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleVO implements Serializable {

    private Long id;
    private String roleCode;
    private String roleName;
    private String description;

    private List<PermissionVO> permissions;

    private List<String> permissionCodes;

    private Long userCount;
}
