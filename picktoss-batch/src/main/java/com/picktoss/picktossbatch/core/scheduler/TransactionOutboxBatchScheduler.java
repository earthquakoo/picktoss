package com.picktoss.picktossbatch.core.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionOutboxBatchScheduler {

//    private final Job transactionOutboxJob;
//    private final JobLauncher jobLauncher;
//
//    @Scheduled(cron = "0 */10 * * * *")
//    public void transactionOutboxJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
//        JobParameters jobParameters = new JobParameters(
//                Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis(), Long.class))
//        );
//        jobLauncher.run(transactionOutboxJob, jobParameters);
//    }
}