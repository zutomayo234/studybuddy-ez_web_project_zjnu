package com.zjnu.studybuddy.mapper;

import com.zjnu.studybuddy.entity.Permission;
import com.zjnu.studybuddy.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    List<Role> selectAll();

    Role selectById(@Param("id") Long id);

    List<Permission> selectPermsByRoleId(@Param("roleId") Long roleId);

    int deleteRolePerms(@Param("roleId") Long roleId);

    int insertRolePerms(@Param("roleId") Long roleId, @Param("permIds") List<Long> permIds);

    long countUsersByRoleId(@Param("roleId") Long roleId);

    long countAll();
}
