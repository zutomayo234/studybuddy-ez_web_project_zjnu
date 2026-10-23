package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StatsOverviewVO implements Serializable {

    private Long userTotal;

    private Long userActive;

    private Long roleCount;

    private Long permCount;

    private Long sessionTotal;

    private Long messageTotal;

    private Integer kbChunkCount;

    private Long logTotal;

    private Long logFailed;

    private List<NameCountVO> roleDistribution;
}
