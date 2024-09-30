package com.picktoss.picktossbatch.core.scheduler;

import com.picktoss.picktossbatch.core.config.job.JobLauncherConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final Job transactionOutboxJob;
    private final Job emailSenderJob;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobLauncherConfig jobLauncherConfig;

    private final String JOB_NAME = "emailSenderJob";

    @Scheduled(cron = "0 */30 * * * *")
    public void transactionOutboxJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        JobParameters jobParameters = new JobParameters(
                Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis(), Long.class))
        );
        jobLauncher.run(transactionOutboxJob, jobParameters);
    }

    @Scheduled(cron = "0 0/5 16-17 * * *") // 00:00부터 01:00까지 20분마다
//    @Scheduled(cron = "0 */2 * * * *")
    public void emailSendJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName(JOB_NAME, 0, 1);
        if (!jobInstances.isEmpty()) {
            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(JOB_NAME);
            assert lastJobInstance != null;
            JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
            assert lastJobExecution != null;
            if (lastJobExecution.getStatus() == BatchStatus.FAILED) {
                try {
                    jobLauncherConfig.restartFailedJob(lastJobExecution.getId());
                } catch (Exception e) {
                    System.out.println("batch processor error: " + e);
                }
            } else {
                JobParameters jobParameters = new JobParameters(
                        Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis(), Long.class))
                );
                jobLauncher.run(emailSenderJob, jobParameters);
            }
        }
    }
}