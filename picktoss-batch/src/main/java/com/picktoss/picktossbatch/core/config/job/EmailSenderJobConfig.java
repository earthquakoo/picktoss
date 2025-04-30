package com.picktoss.picktossbatch.core.config.job;

import com.picktoss.picktossbatch.core.config.listener.JobListener;
import com.picktoss.picktossbatch.core.config.listener.StepListener;
import com.picktoss.picktossbatch.core.config.partitioner.CustomPartitioner;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizBatchService;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan(basePackages = {"com.picktoss.picktossserver"})
public class EmailSenderJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final CustomPartitioner customPartitioner;
    private final JobListener jobListener;
    private final StepListener stepListener;
    private final QuizBatchService quizChunkBatchInsert;
    private final RedisUtil redisUtil;

    private final String JOB_NAME = "emailSenderJob";
    private final String STEP_NAME = "masterStep";

    private long itemStartTime;
    private long itemEndTime;

    @Bean(name = JOB_NAME)
    public Job emailSenderJob(JobRepository jobRepository, Step masterStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(jobListener)
                .incrementer(new RunIdIncrementer())
                .start(masterStep)
                .build();
    }

    @Bean
    public Step masterStep(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .partitioner("slaveStep", customPartitioner)
                .step(slaveStep(jobRepository, transactionManager))
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step slaveStep(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .listener(stepListener)
                .<Member, Member>chunk(2500, transactionManager)
                .reader(itemReader(null, null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> itemReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end
    ) {
        long readerDurationStartTime = System.currentTimeMillis();
        itemStartTime = System.currentTimeMillis();
        JpaPagingItemReader<Member> jpaPagingItemReader = new JpaPagingItemReader<>();
        jpaPagingItemReader.setQueryString(
                "SELECT m FROM Member m " +
                        "JOIN FETCH m.directories dir " +
                        "JOIN FETCH dir.documents d " +
                        "JOIN FETCH d.quizzes " +
                        "WHERE m.id >= :start and m.id <= :end"
        );
        HashMap<String, Object> map = new HashMap<>();
        map.put("start", start);
        map.put("end", end);
        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(2500);
        long readerDurationEndTime = System.currentTimeMillis();
        long duration = readerDurationEndTime - readerDurationStartTime;
        System.out.println("Reader Duration time: " + duration);
        return jpaPagingItemReader;
    }

    @Bean
    @StepScope
    public ItemWriter<Member> itemWriter() {
        long startTime = System.currentTimeMillis();
        return chunk -> {
            List<Quiz> quizListToUpdate = new ArrayList<>();
            List<QuizSet> quizSets = new ArrayList<>();
            List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
            List<Member> members = new ArrayList<>();
            for (Member member : chunk.getItems()) {
                if (member == null || member.getDirectories() == null) {
                    continue;
                }
                members.add(member);

                List<Quiz> quizzesBySortedDeliveredCount = new ArrayList<>();
                Set<Directory> directories = member.getDirectories();
                for (Directory directory : directories) {
                    if (directory.getDocuments() == null || directory.getDocuments().isEmpty()) {
                        continue;
                    }
                    Set<Document> documents = directory.getDocuments();
                    for (Document document : documents) {
                        if (document.getQuizzes() == null || document.getQuizzes().isEmpty()) {
                            continue;
                        }
                        Set<Quiz> quizzes = document.getQuizzes();

                        List<Quiz> quizList = quizzes.stream()
                                .sorted(Comparator.comparingInt(Quiz::getDeliveredCount))
                                .toList();

                        quizzesBySortedDeliveredCount.addAll(quizList);
                    }
                }
                QuizSet quizSet = QuizSet.createQuizSet("오늘의 퀴즈 세트", member);
                quizSets.add(quizSet);

                int quizCount = 0;

                for (Quiz quiz : quizzesBySortedDeliveredCount) {
                    QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
                    quizSetQuizzes.add(quizSetQuiz);
                    quizListToUpdate.add(quiz);
                    quizCount += 1;
                    if (quizCount == 10) {
                        break;
                    }
                }
            }

            try {
                quizChunkBatchInsert.quizChunkBatchInsert(quizListToUpdate, quizSets, quizSetQuizzes, members);
            } catch (DataAccessException dataAccessException) {
                throw dataAccessException;
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Writer duration time: " + duration);
            itemEndTime = System.currentTimeMillis();
            long itemDurationTime = itemEndTime - itemStartTime;
            System.out.println("Total item duration time: " + itemDurationTime);
        };
    }
}