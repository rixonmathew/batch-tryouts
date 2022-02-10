package com.rixon.batch.fileprocessorh2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(BatchConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("${file.input}")
    private String fileInput;


    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("async_executor");
    }

    @Bean
    public FlatFileItemReader<Coffee> coffeeFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<Coffee>().name("cofferItemReader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .names("brand","origin","characteristics")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>(){{
                    setTargetType(Coffee.class);
                }})
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Coffee> coffeeJdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Coffee>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into coffee(brand,origin,characteristics) values (:brand,:origin,:characteristics)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public CoffeeItemProcessor coffeeItemProcessor() {
        return new CoffeeItemProcessor();
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {

            }
            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                LOGGER.info("Processed [{}] rows ",stepExecution.getCommitCount());
                return ExitStatus.EXECUTING;
            }
        };
    }

    @Bean
    public Step step(JdbcBatchItemWriter<Coffee> coffeeJdbcBatchItemWriter,TaskExecutor taskExecutor,StepExecutionListener stepExecutionListener) {
        return stepBuilderFactory
                .get("step1")
                .<Coffee,Coffee> chunk(10)
                .reader(coffeeFlatFileItemReader())
                .processor(coffeeItemProcessor())
                .writer(coffeeJdbcBatchItemWriter)
                .taskExecutor(taskExecutor)
                .listener(stepExecutionListener)
                .build();
    }

    @Bean
    public JobExecutionListener jobExecutionListener(DataSource dataSource) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                LOGGER.info("Starting job [{}]",jobExecution.getJobId());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {

                if (jobExecution.getStatus()==BatchStatus.COMPLETED) {
                    LOGGER.info("Finished job [{}] at [{}]",jobExecution.getJobId(),jobExecution.getEndTime());
                    LOGGER.info("Checking count of rows created");
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    Integer count = jdbcTemplate.queryForObject("select count(1) from coffee", Integer.class);
                    LOGGER.info("Found [{}] rows", count);
                }

            }
        };
    }

    @Bean
    public Job importUserJob(JobExecutionListener jobExecutionListener,Step step) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .flow(step)
                .end().build();
    }
}
