package com.zjnu.studybuddy.mapper;

import com.zjnu.studybuddy.dto.LogQueryDTO;
import com.zjnu.studybuddy.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    int insert(OperationLog log);

    List<OperationLog> selectPage(@Param("q") LogQueryDTO query);

    long countByQuery(@Param("q") LogQueryDTO query);

    long countFailed();
}
