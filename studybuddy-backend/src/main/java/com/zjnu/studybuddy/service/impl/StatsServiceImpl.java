package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.ai.RagService;
import com.zjnu.studybuddy.mapper.ChatMessageMapper;
import com.zjnu.studybuddy.mapper.ChatSessionMapper;
import com.zjnu.studybuddy.mapper.OperationLogMapper;
import com.zjnu.studybuddy.mapper.PermissionMapper;
import com.zjnu.studybuddy.mapper.RoleMapper;
import com.zjnu.studybuddy.mapper.UserMapper;
import com.zjnu.studybuddy.service.StatsService;
import com.zjnu.studybuddy.vo.StatsOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OperationLogMapper logMapper;
    private final RagService ragService;

    @Override
    public StatsOverviewVO overview() {
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setUserTotal(userMapper.countAll());
        vo.setUserActive(userMapper.countByStatus(1));
        vo.setRoleCount(roleMapper.countAll());
        vo.setPermCount(permissionMapper.countAll());
        vo.setSessionTotal(sessionMapper.countAll());
        vo.setMessageTotal(messageMapper.countAll());
        vo.setKbChunkCount(ragService.chunkCount());
        vo.setLogTotal(logMapper.countByQuery(newLogQuery()));
        vo.setLogFailed(logMapper.countFailed());
        vo.setRoleDistribution(userMapper.countByRole());
        return vo;
    }

    private com.zjnu.studybuddy.dto.LogQueryDTO newLogQuery() {
        return new com.zjnu.studybuddy.dto.LogQueryDTO();
    }
}
