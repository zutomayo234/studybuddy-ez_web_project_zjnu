package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.entity.Permission;
import com.zjnu.studybuddy.vo.RoleVO;

import java.util.List;

public interface RoleService {

    List<RoleVO> listRoles();

    List<Permission> listPermissions();

    void updateRolePermissions(Long roleId, List<Long> permIds);
}
