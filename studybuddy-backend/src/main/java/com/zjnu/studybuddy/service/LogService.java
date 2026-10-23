package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.common.PageResult;
import com.zjnu.studybuddy.dto.LogQueryDTO;
import com.zjnu.studybuddy.entity.OperationLog;

public interface LogService {

    void record(OperationLog log);

    PageResult<OperationLog> page(LogQueryDTO query);
}
