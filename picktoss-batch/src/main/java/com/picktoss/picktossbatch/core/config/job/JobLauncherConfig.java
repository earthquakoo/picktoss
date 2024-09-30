package com.picktoss.picktossbatch.core.config.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobLauncherConfig {

    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

//    @Bean
//    @ConditionalOnMissingBean
//    @ConditionalOnProperty(prefix = "spring.batch.job", name = "enabled", havingValue = "true", matchIfMissing = true)
//    public JobLauncherApplicationRunner jobLauncherApplicationRunner(JobLauncher jobLauncher, JobExplorer jobExplorer, JobRepository jobRepository, BatchProperties properties) {
//
//        JobLauncherApplicationRunner runner = new JobLauncherApplicationRunner(
//                jobLauncher, jobExplorer, jobRepository);
//        String jobName = properties.getJob().getName();
//        if (StringUtils.hasText(jobName)) {
//            runner.setJobName(jobName);
//        }
//        return runner;
//    }

//    public boolean hasFailedJobInstance(String jobName) {
//        List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName(jobName, 0, 1);
//        if (!jobInstances.isEmpty()) {
//            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(jobName);
//            assert lastJobInstance != null;
//            JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
//            assert lastJobExecution != null;
//            if (lastJobExecution.getStatus() == BatchStatus.FAILED) {
//                return true;
//            }
//
//            JobInstance lastJobInstance = jobInstances.get(0);
//            List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(lastJobInstance);
//            for (JobExecution jobExecution : jobExecutions) {
//                if (jobExecution.getStatus() == BatchStatus.FAILED) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void restartFailedJob(long executionId) throws Exception {
        jobOperator.restart(executionId);
    }
}
