package com.picktoss.picktossbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PicktossBatch {

    public static void main(String[] args) {
        SpringApplication.run(PicktossBatch.class, args);
    }
}