package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.common.BusinessException;
import com.zjnu.studybuddy.common.ResultCode;
import com.zjnu.studybuddy.entity.Permission;
import com.zjnu.studybuddy.entity.Role;
import com.zjnu.studybuddy.mapper.PermissionMapper;
import com.zjnu.studybuddy.mapper.RoleMapper;
import com.zjnu.studybuddy.service.RoleService;
import com.zjnu.studybuddy.vo.PermissionVO;
import com.zjnu.studybuddy.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public List<RoleVO> listRoles() {
        return roleMapper.selectAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<Permission> listPermissions() {
        return permissionMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<Long> permIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }

        Set<Long> validIds = permissionMapper.selectAll().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        List<Long> illegal = permIds.stream().filter(id -> !validIds.contains(id)).toList();
        if (!illegal.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "存在非法的权限 ID：" + illegal);
        }

        roleMapper.deleteRolePerms(roleId);
        roleMapper.insertRolePerms(roleId, permIds.stream().distinct().toList());
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        List<Permission> perms = roleMapper.selectPermsByRoleId(role.getId());
        vo.setPermissions(perms.stream().map(p -> {
            PermissionVO pv = new PermissionVO();
            BeanUtils.copyProperties(p, pv);
            return pv;
        }).collect(Collectors.toList()));
        vo.setPermissionCodes(perms.stream().map(Permission::getPermCode).toList());
        vo.setUserCount(roleMapper.countUsersByRoleId(role.getId()));
        return vo;
    }
}
