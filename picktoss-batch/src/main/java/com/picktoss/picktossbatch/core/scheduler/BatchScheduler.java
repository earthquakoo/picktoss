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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final Job transactionOutboxJob;
    private final Job emailSenderJob;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobLauncherConfig jobLauncherConfig;

    private final String JOB_NAME = "emailSenderJob";

    @Scheduled(cron = "0 */10 * * * *")
    public void transactionOutboxJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        JobParameters jobParameters = new JobParameters(
                Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis(), Long.class))
        );
        jobLauncher.run(transactionOutboxJob, jobParameters);
    }

    @Scheduled(cron = "0 0/5 2-3 * * *") // 00:00부터 01:00까지 20분마다
//    @Scheduled(cron = "0 */2 * * * *")
    public void emailSendJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        JobInstance lastJobInstance = jobExplorer.getLastJobInstance(JOB_NAME);

        if (lastJobInstance != null) {
            JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);

            // 마지막 작업의 실행 완료 시간을 확인
            if (lastJobExecution != null && lastJobExecution.getStatus() == BatchStatus.COMPLETED) {
                LocalDate today = LocalDate.now();
                LocalDateTime startTime = LocalDateTime.of(today, LocalTime.of(2, 0));
                LocalDateTime endTime = LocalDateTime.of(today, LocalTime.of(3, 0));
                LocalDate lastEndDate = lastJobExecution.getEndTime().toLocalDate();
                LocalTime lastEndTime = lastJobExecution.getEndTime().toLocalTime();

                LocalDateTime lastJobEndTime = LocalDateTime.of(lastEndDate, lastEndTime);

                // 작업 완료 시간이 01:00 ~ 02:00 사이에 있으면 배치 실행하지 않음
                if (lastJobEndTime.isAfter(startTime) && lastJobEndTime.isBefore(endTime)) {
                    System.out.println("오늘 01:00-02:00 사이에 이미 완료된 배치 작업이 있습니다.");
                    return;
                }
            }
        }

        JobParameters jobParameters = new JobParameters(
                Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis(), Long.class))
        );
        jobLauncher.run(emailSenderJob, jobParameters);
    }
}