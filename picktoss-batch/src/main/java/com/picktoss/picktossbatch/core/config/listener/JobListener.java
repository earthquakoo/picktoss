package com.picktoss.picktossbatch.core.config.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobListener implements JobExecutionListener {

    private long startTime;
    private long endTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = System.currentTimeMillis();
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Job Duration time: " + duration + " milliseconds.");
        JobExecutionListener.super.afterJob(jobExecution);
    }
}
