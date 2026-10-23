package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class KbStatsVO implements Serializable {

    private Integer chunkCount;

    private Integer runtimeChunkCount;

    private String sourceFile;

    private Integer topK;
}
