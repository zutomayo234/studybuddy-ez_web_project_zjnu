package com.zjnu.studybuddy.mapper;

import com.zjnu.studybuddy.dto.UserQueryDTO;
import com.zjnu.studybuddy.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> selectPage(@Param("q") UserQueryDTO query);

    long countByQuery(@Param("q") UserQueryDTO query);

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    int countByUsername(@Param("username") String username);

    int insert(User user);

    int updateById(User user);

    int deleteById(@Param("id") Long id);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int deleteUserRoles(@Param("userId") Long userId);

    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    long countAll();

    long countByStatus(@Param("status") Integer status);

    List<com.zjnu.studybuddy.vo.NameCountVO> countByRole();
}
