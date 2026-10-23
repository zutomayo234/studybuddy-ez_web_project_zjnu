package com.zjnu.studybuddy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameCountVO implements Serializable {

    private String name;

    private Long value;
}
