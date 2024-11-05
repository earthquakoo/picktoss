package com.picktoss.picktossbatch.core.config.job;

import com.picktoss.picktossserver.core.event.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.event.publisher.sqs.SQSEventMessagePublisher;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import com.picktoss.picktossserver.domain.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan(basePackages = {"com.picktoss.picktossserver"})
public class TransactionOutboxJobConfig {

    private final OutboxService outboxService;
    private final SQSEventMessagePublisher sqsEventMessagePublisher;

    private final String JOB_NAME = "transactionOutboxJob";
    private final String STEP_NAME = "transactionOutboxStep";

    @Bean(name = JOB_NAME)
    public Job transactionOutboxJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer()) // sequential id
                .start(transactionOutboxStep(jobRepository, transactionManager)) // step 설정
                .build();
    }

    @Bean
    @JobScope
    public Step transactionOutboxStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(transactionOutboxTasklet(), transactionManager) // tasklet 설정
                .build();
    }

    @Bean
    @StepScope
    public Tasklet transactionOutboxTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                List<Outbox> outboxes = outboxService.findAllOutbox();
                for (Outbox outbox : outboxes) {
                    if (outbox.getTryCount() >= 5) {
                        outbox.updateOutboxStatusByBatchFailed();
                        return RepeatStatus.FINISHED;
                    }
                    Document document = outbox.getDocument();
                    Member member = document.getDirectory().getMember();
                    document.updateDocumentStatusProcessingByGenerateAiPick();
                    outbox.addTryCountBySendMessage();
                    sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(member.getId(), document.getS3Key(), document.getId(), outbox.getCreatedQuizType(), outbox.getUsedStars()));
                }
                return RepeatStatus.FINISHED;
            }
        };
    }
}