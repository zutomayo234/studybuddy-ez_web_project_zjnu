package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.common.PageResult;
import com.zjnu.studybuddy.dto.LogQueryDTO;
import com.zjnu.studybuddy.entity.OperationLog;
import com.zjnu.studybuddy.mapper.OperationLogMapper;
import com.zjnu.studybuddy.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final OperationLogMapper logMapper;

    @Override
    public void record(OperationLog operationLog) {
        try {
            logMapper.insert(operationLog);
        } catch (Exception e) {

            log.warn("写入操作日志失败（不影响主流程）：{}", e.getMessage());
        }
    }

    @Override
    public PageResult<OperationLog> page(LogQueryDTO query) {
        long total = logMapper.countByQuery(query);
        List<OperationLog> rows = total == 0 ? List.of() : logMapper.selectPage(query);
        return new PageResult<>(query.getPageNum(), query.getPageSize(), total, rows);
    }
}
