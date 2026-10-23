package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.vo.KbChunkVO;
import com.zjnu.studybuddy.vo.KbStatsVO;

import java.util.List;

public interface KbService {

    List<KbChunkVO> listChunks();

    KbStatsVO stats();

    int reload();

    int addChunk(String content);
}
