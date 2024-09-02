package com.picktoss.picktosssendemailbatch.core.config;

import com.picktoss.picktosssendemailbatch.core.config.dto.ItemWriterDto;
import com.picktoss.picktossserver.core.email.MailgunTodayQuizSetEmailManager;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan(basePackages = {"com.picktoss.picktossserver"})
public class BatchConfig {

    private final MailgunTodayQuizSetEmailManager mailgunTodayQuizSetEmailManager;
    private final EntityManagerFactory entityManagerFactory;
    private final CustomPartitioner customPartitioner;
    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;

    private final String JOB_NAME = "sendEmailJob";
    private final String STEP_NAME = "masterStep";

    /**
     * Job 등록
     */
    @Bean
    public Job sendEmailJob(JobRepository jobRepository, Step masterStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(masterStep)
                .build();
    }

    @Bean
    public Step masterStep(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        System.out.println("================masterStep Start==================");
        return new StepBuilder(STEP_NAME, jobRepository)
                .partitioner("slaveStep", customPartitioner)
                .step(slaveStep(jobRepository, transactionManager))
                .gridSize(5)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step slaveStep(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        System.out.println("================slaveStep Start==================");
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Member, ItemWriterDto>chunk(5, transactionManager)
                .reader(itemReader(null, null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> itemReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end
    ) {
        System.out.println("================itemReader Start==================");
        JpaPagingItemReader<Member> jpaPagingItemReader = new JpaPagingItemReader<>();
        jpaPagingItemReader.setQueryString(
                "SELECT m FROM Member m WHERE m.id >= :start and m.id <= :end"
        );
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", start);
        map.put("end", end);
        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(5);
        return jpaPagingItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, ItemWriterDto> itemProcessor() {
        System.out.println("================itemProcessor Start==================");
        return Member -> {
            List<Quiz> quizzes = quizRepository.findAllByMemberIdOrderByDeliveredCountASC(Member.getId());
            List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

            String quizSetId = UUID.randomUUID().toString().replace("-", "");
            QuizSet quizSet = QuizSet.createQuizSet(quizSetId, true, Member);

            int quizCount = 0;

            for (Quiz quiz : quizzes) {
                if (quizCount == 10) {
                    break;
                }

                QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
                quizSetQuizzes.add(quizSetQuiz);
                quizCount += 1;
            }
//            mailgunTodayQuizSetEmailManager.sendTodayQuizSet("cream5343@gmail.com", Member.getName());

            return ItemWriterDto.builder()
                    .quizSetQuizzes(quizSetQuizzes)
                    .quizSet(quizSet)
                    .member(Member)
                    .build();
        };
    }

    @Bean
    @StepScope
    public ItemWriter<ItemWriterDto> itemWriter() {
        System.out.println("================itemWrite Start==================");
        long startTime = System.currentTimeMillis();
        return chunk -> {
            for (ItemWriterDto itemWriterDto : chunk.getItems()) {
                List<QuizSetQuiz> quizSetQuizzes = itemWriterDto.getQuizSetQuizzes();
                for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                    Quiz quiz = quizSetQuiz.getQuiz();
                    quiz.addDeliveredCount();
                }
                quizSetRepository.save(itemWriterDto.getQuizSet());
                quizSetQuizRepository.saveAll(itemWriterDto.getQuizSetQuizzes());
            }
            long endTime = System.currentTimeMillis();
            System.out.println("ItemWriter 총 걸린 시간(chunkSize: 5) ");
            System.out.println(endTime - startTime);
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(5 * 2);
        taskExecutor.setThreadNamePrefix("async-thread");
        return taskExecutor;
    }
}