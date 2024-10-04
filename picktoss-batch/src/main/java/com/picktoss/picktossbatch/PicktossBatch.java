package com.picktoss.picktossbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class PicktossBatch {

    public static void main(String[] args) {
        SpringApplication.run(PicktossBatch.class, args);
    }
}