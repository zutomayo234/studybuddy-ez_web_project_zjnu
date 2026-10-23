package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.annotation.OpLog;
import com.zjnu.studybuddy.common.PageResult;
import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.dto.RoleAssignDTO;
import com.zjnu.studybuddy.dto.UserCreateDTO;
import com.zjnu.studybuddy.dto.UserQueryDTO;
import com.zjnu.studybuddy.dto.UserUpdateDTO;
import com.zjnu.studybuddy.service.UserService;
import com.zjnu.studybuddy.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理", description = "用户 CRUD、重置密码与角色分配（实验一 / 实验四）")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户", description = "支持按用户名/昵称/邮箱关键字与状态筛选")
    @GetMapping
    @PreAuthorize("hasAuthority('user:list') or hasRole('ADMIN')")
    public Result<PageResult<UserVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        UserQueryDTO query = new UserQueryDTO();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return Result.success(userService.page(query));
    }

    @Operation(summary = "查询用户详情", description = "返回单个用户信息（手机号已脱敏）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:list') or hasRole('ADMIN')")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "新增用户", description = "密码经 BCrypt 加密后入库，默认分配学生角色")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create') or hasRole('ADMIN')")
    @OpLog(module = "用户管理", action = "新增用户")
    public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success("新增成功", userService.create(dto));
    }

    @Operation(summary = "更新用户", description = "按主键更新用户基础信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    @OpLog(module = "用户管理", action = "更新用户")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return Result.success("更新成功", userService.update(id, dto));
    }

    @Operation(summary = "删除用户", description = "逻辑删除（deleted=1），保留历史数据可追溯")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ADMIN')")
    @OpLog(module = "用户管理", action = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "重置密码", description = "管理员对指定用户重置密码（无需旧密码）")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    @OpLog(module = "用户管理", action = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String password) {
        userService.resetPassword(id, password);
        return Result.success("密码已重置", null);
    }

    @Operation(summary = "分配角色", description = "覆盖式更新用户的角色集合；内置「最后一名管理员不可降级」保护")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('role:assign') or hasRole('ADMIN')")
    @OpLog(module = "用户管理", action = "分配角色")
    public Result<UserVO> assignRoles(@PathVariable Long id,
                                      @Valid @RequestBody RoleAssignDTO dto) {
        return Result.success("角色已更新", userService.assignRoles(id, dto.getRoleIds()));
    }
}
