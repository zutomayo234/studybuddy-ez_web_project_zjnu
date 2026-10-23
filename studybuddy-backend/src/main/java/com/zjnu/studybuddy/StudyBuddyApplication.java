package com.zjnu.studybuddy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zjnu.studybuddy.mapper")
public class StudyBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyBuddyApplication.class, args);
    }
}
