package com.zjnu.studybuddy.mapper;

import com.zjnu.studybuddy.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    List<Permission> selectAll();

    long countAll();
}
