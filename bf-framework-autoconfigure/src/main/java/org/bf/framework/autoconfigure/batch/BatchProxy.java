package org.bf.framework.autoconfigure.batch;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public class BatchProxy {
    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private JobOperator jobOperator;
    private PlatformTransactionManager transactionManager;
    private DataSource dataSource;

    private JobRegistry jobRegistry;

    private JobExplorer jobExplorer;

    public JobRepository getJobRepository() {
        return jobRepository;
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public JobOperator getJobOperator() {
        return jobOperator;
    }

    public void setJobOperator(JobOperator jobOperator) {
        this.jobOperator = jobOperator;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public JobRegistry getJobRegistry() {
        return jobRegistry;
    }

    public void setJobRegistry(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public JobExplorer getJobExplorer() {
        return jobExplorer;
    }

    public void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

}
