package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.annotation.OpLog;
import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.dto.RolePermissionDTO;
import com.zjnu.studybuddy.entity.Permission;
import com.zjnu.studybuddy.service.RoleService;
import com.zjnu.studybuddy.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "角色权限", description = "RBAC 角色与权限的管理端接口（功能增强）")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询角色列表", description = "返回全部角色，并附带各自的权限明细与挂载用户数")
    @GetMapping
    @PreAuthorize("hasAuthority('role:list') or hasRole('ADMIN')")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.listRoles());
    }

    @Operation(summary = "查询权限点全集", description = "前端权限勾选树的候选数据")
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role:list') or hasRole('ADMIN')")
    public Result<List<Permission>> permissions() {
        return Result.success(roleService.listPermissions());
    }

    @Operation(summary = "调整角色权限", description = "以全量覆盖的方式更新该角色的权限集合")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:assign') or hasRole('ADMIN')")
    @OpLog(module = "角色权限", action = "调整角色权限")
    public Result<Void> updatePermissions(@PathVariable Long id,
                                          @Valid @RequestBody RolePermissionDTO dto) {
        roleService.updateRolePermissions(id, dto.getPermIds());
        return Result.success("角色权限已更新", null);
    }
}
