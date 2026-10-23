package com.zjnu.studybuddy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionDTO {

    @NotEmpty(message = "至少保留一个权限")
    private List<Long> permIds;
}
