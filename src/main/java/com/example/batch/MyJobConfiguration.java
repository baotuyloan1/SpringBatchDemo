package com.example.batch;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * a job has one to many steps, each of which has exactly one ItemReader, On ItemPrecessor and one ItemWriter. A job needs to be lauched (with JobLauncher) and metadata about the currenly rynning process need to be sotred (JobReposiutory)
 * <p>
 * A job is an entity that encapsulates an entire batch process. Job is only the top of an overall hierarchy
 * <p>
 * A job is simply container for Step instances. It combines multiple steps that logically belong together in a flow. The job configuration contains:
 * - THe name of the job.
 * - Deifinition and ordering of the Step instances.
 * - Whether or not the job is restartable.
 * <p>
 * 3.1.1 JobInstance
 * A job instance refers to aconcept of logical job run,
 */
@Configuration
//co 2 cach EnableBatchProcessing hoac extends ra
//customize dataSource and transactionManager
@EnableBatchProcessing(dataSourceRef = "batchDataSource", transactionManagerRef = "batchTransactionManager")
//batchDataSource and batchTransactionManager refer to beans in the application context
//use default dataSource and transactionManager
//Only one configuration class needs to have the @EnableBatchProcessing annotation. Once you have a class annotated with it, you have all of the configuration described earlier.
/**
 *
 @EnableBatchProcessing should not be used with DefaultBatchConfiguration. You should either use the declarative way of configuring Spring Batch through @EnableBatchProcessing, or use the programmatic way of extending DefaultBatchConfiguration, but not both ways at the same time.
 */
public class MyJobConfiguration extends DefaultBatchConfiguration {

    /*
     *

     * JobRepository: a bean named jobRepository

     * JobLauncher: a bean named jobLauncher

     * JobRegistry: a bean named jobRegistry

     *  JobExplorer: a bean named jobExplorer

     * JobOperator: a bean named jobOperator

     */

    @Bean
    public DataSource batchDataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
                .addScript("/org/springframework/batch/core/schema-hsqldb.sql")
                .generateUniqueName(true).build();
    }

    @Bean
    public JdbcTransactionManager batchTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }


    // customize dataSource and transactionManager

    // if a job should never be restarted but should always be run as part of a new JobInstance, you can set restartable property to false;
    @Bean
    public Job footballJob(JobRepository jobRepository) {
        return new JobBuilder("footballJob", jobRepository) //
                .preventRestart() //
                .validator(parametersValidator()) // validator for the job parameters at runtime
                .listener(sampleListener()) //
                .start(playLoad()) //
                .next(gameLoad()) //
                .next(playerSummarization()) //
                .build();
    }

    /**
     * Note that the afterJob method is called regardless of the success or failure of the Job. If you need to determine success or failure, you can get that information from the JobExecution:
     *
     * @param jobExecution
     */
    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            //job success
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            //job failure
        }
    }


}
