package com.picktoss.picktosssendemailbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PicktossSendEmailBatch {

    public static void main(String[] args) {
        SpringApplication.run(PicktossSendEmailBatch.class, args);
    }
}