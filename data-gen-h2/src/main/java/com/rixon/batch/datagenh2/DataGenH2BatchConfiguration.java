package com.rixon.batch.datagenh2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableBatchProcessing
public class DataGenH2BatchConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataGenH2BatchConfiguration.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private final long MAX_INSTRUMENT_COUNT = 1_000_000L;

    @Autowired
    private InstrumentGenerator instrumentGenerator;

    @Autowired
    private AccountGenerator accountGenerator;

    @Bean
    @Primary
    public JobLauncher asyncJobLauncher() throws Exception
    {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();

        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor("async_jobs"));
        jobLauncher.afterPropertiesSet();

        return jobLauncher;
    }

    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("async_executor");
    }


    @Bean
    public ItemStreamReader<Instrument> instrumentIteratorItemReader(){
        return new ItemStreamReader<>() {
            private final AtomicLong counter = new AtomicLong();

            @Override
            public Instrument read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                if (counter.get()>= MAX_INSTRUMENT_COUNT){
                    LOGGER.info("No more instruments to generated");
                    return null;
                }
                return instrumentGenerator.randomInstrument(counter.incrementAndGet());
            }

            @Override
            public void open(ExecutionContext executionContext) throws ItemStreamException {

            }

            @Override
            public void update(ExecutionContext executionContext) throws ItemStreamException {

            }

            @Override
            public void close() throws ItemStreamException {

            }
        };
    }


    @Bean
    public ItemStreamReader<Account> accountItemStreamReader(){
        return new ItemStreamReader<>() {
            private final AtomicLong counter = new AtomicLong();

            @Override
            public Account read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                if (counter.get()>= MAX_INSTRUMENT_COUNT){
                    LOGGER.info("No more accounts to generated");
                    return null;
                }
                return accountGenerator.randomAccount(counter.incrementAndGet());
            }

            @Override
            public void open(ExecutionContext executionContext) throws ItemStreamException {

            }

            @Override
            public void update(ExecutionContext executionContext) throws ItemStreamException {

            }

            @Override
            public void close() throws ItemStreamException {

            }
        };
    }


    @Bean
    public JdbcBatchItemWriter<Instrument> instrumentJdbcBatchWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Instrument>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into instrument(id,type,name,price,inceptionDate,createdBy,createdTime,updatedBy,updatedTime,version) values " +
                        "(:id,:type,:name,:price,:inceptionDate,:createdBy,:createdTime,:updatedBy,:updatedTime,:version)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Account> accountJdbcBatchWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Account>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into account(id,balance,clientId,type,active,createdBy,createdTime,updatedBy,updatedTime,version) values " +
                        "(:id,:balance,:clientId,:type,:active,:createdBy,:createdTime,:updatedBy,:updatedTime,:version)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step instrumentLoadStep(JdbcBatchItemWriter<Instrument> instrumentJdbcBatchItemWriter, TaskExecutor taskExecutor) {
        return stepBuilderFactory
                .get("step1")
                .<Instrument,Instrument> chunk(1000)
                .reader(instrumentIteratorItemReader())
                .writer(instrumentJdbcBatchItemWriter)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step accountLoadStep(JdbcBatchItemWriter<Account> accountJdbcBatchItemWriter, TaskExecutor taskExecutor) {
        return stepBuilderFactory
                .get("step2")
                .<Account,Account> chunk(1000)
                .reader(accountItemStreamReader())
                .writer(accountJdbcBatchItemWriter)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public JobExecutionListener instrumentJobExecutionListener(DataSource dataSource) {
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
                    Integer count = jdbcTemplate.queryForObject("select count(1) from instrument", Integer.class);
                    LOGGER.info("Found [{}] rows", count);
                }

            }
        };
    }

    @Bean
    public JobExecutionListener accountImportJobExecutionListener(DataSource dataSource) {
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
                    Integer count = jdbcTemplate.queryForObject("select count(1) from account", Integer.class);
                    LOGGER.info("Found [{}] rows", count);
                }

            }
        };
    }

    @Bean
    public Job instrumentLoadJob(@Qualifier("instrumentJobExecutionListener") JobExecutionListener jobExecutionListener, @Qualifier("instrumentLoadStep") Step instrumentLoadStep) {
        return jobBuilderFactory.get("instrumentLoadJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .flow(instrumentLoadStep)
                .end().build();
    }

    @Bean
    public Job accountLoadJob(@Qualifier("accountImportJobExecutionListener") JobExecutionListener jobExecutionListener, @Qualifier("accountLoadStep") Step instrumentLoadStep) {
        return jobBuilderFactory.get("accountLoadJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .flow(instrumentLoadStep)
                .end().build();
    }
}
