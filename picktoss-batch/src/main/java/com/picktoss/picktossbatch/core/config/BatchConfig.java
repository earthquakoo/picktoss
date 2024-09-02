package com.picktoss.picktossbatch.core.config;

import com.picktoss.picktossserver.core.event.event.SQSEvent;
import com.picktoss.picktossserver.core.event.publisher.SQSEventMessagePublisher;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import com.picktoss.picktossserver.domain.outbox.service.OutboxService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
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
public class BatchConfig {

    private final OutboxService outboxService;
    private final SubscriptionService subscriptionService;
    private final SQSEventMessagePublisher sqsEventMessagePublisher;

    private final String JOB_NAME = "transactionOutboxJob";
    private final String STEP_NAME = "transactionOutboxStep";

    /**
     * Job 등록
     */
    @Bean
    public Job transactionOutboxJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer()) // sequential id
                .start(transactionOutboxStep(jobRepository, transactionManager)) // step 설정
                .build();
    }

    /**
     * Step 등록
     */
    @Bean
    @JobScope
    public Step transactionOutboxStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(transactionOutboxTasklet(), transactionManager) // tasklet 설정
                .build();
    }

    /**
     * Tasklet: Reader-Processor-Writer를 구분하지 않는 단일 step
     */
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
                    Member member = document.getCategory().getMember();
                    Subscription subscription = subscriptionService.findCurrentSubscription(member.getId(), member);
                    document.updateDocumentStatusProcessingByGenerateAiPick();
                    outbox.addTryCountBySendMessage();
                    sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSEvent(member.getId(), document.getS3Key(), document.getId(), subscription.getSubscriptionPlanType()));
                }
                return RepeatStatus.FINISHED;
            }
        };
    }
}