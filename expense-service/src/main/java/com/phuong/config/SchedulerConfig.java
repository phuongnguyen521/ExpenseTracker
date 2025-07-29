package com.phuong.config;

import com.phuong.constants.ExecutorConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import lombok.extern.slf4j.Slf4j;

@Slf4jj
@Configurationtion
@EnableScheduling
public class SchedulerConfig {

    @Bean(name = ExecutorConstants.TASK_SCHEDULE) 
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("Scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRejectedExecutionHandler((r, excutor) -> {
            log.warn("Scheduled task rejected: {}", r.toString());
        });
        scheduler.setErrorHandler(throwable -> {
            log.error("Schedule task error", throwable);
        });
        scheduler.initialize();

        log.info("Initialized task scheduler with pool with pool size: {}", scheduler.getPoolSize());
        return scheduler;
    }
    
}
