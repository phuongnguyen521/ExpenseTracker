package com.phuong.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.phuong.constants.ExecutorConstants;
import com.phuong.repository.ExpenseRepository;
import com.phuong.service.AsyncExpenseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseReportScheduler {
    
    private final ExpenseRepository expenseRepository;
    private final AsyncExpenseService asyncExpenseService;

    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    @Async(ExecutorConstants.TASK_SCHEDULE)
    public void generateDailyReports() {
        log.info("Starting daily expense report generation at {}", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            List<Long> activeUsers = expenseRepository.findDistinctUserIds();

            log.info("Generating daily reports for {} active users", activeUsers.size());
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(1);

            CompletableFuture<?>[] futures = activeUsers.stream()
            .map(userId -> 
            asyncExpenseService.getExpenseSummaryAsync(userId, startDate, endDate))
            .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures)
            .whenComplete((result, throwable) -> {
                if (throwable == null) {
                    log.info("Daily report generation completed successfully for all users");
                    // TODO: Generate json file
                } else {
                    log.info("Daily report generation failed for some users", throwable);
                }
            });
        } catch (Exception ex) {
            log.error("Error during daily report generation", ex);
        }
    }
}
