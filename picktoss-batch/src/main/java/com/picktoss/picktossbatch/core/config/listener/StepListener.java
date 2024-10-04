package com.picktoss.picktossbatch.core.config.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class StepListener implements StepExecutionListener {

    private long startTime;
    private long endTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        startTime = System.currentTimeMillis();
        StepExecutionListener.super.beforeStep(stepExecution);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Step Duration time: " + duration + " milliseconds.");
        return StepExecutionListener.super.afterStep(stepExecution);
    }
}
