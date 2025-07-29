package com.phuong.service.impl;

import com.phuong.constants.ExecutorConstants;
import com.phuong.dto.request.ExpenseRequest;
import com.phuong.dto.response.ExpenseReport;
import com.phuong.dto.response.ExpenseResponse;
import com.phuong.dto.response.ExpenseSummary;
import com.phuong.exception.BusinessRuleException;
import com.phuong.exception.ExternalServiceException;
import com.phuong.exception.ResourceNotFoundException;
import com.phuong.mapper.ExpenseMapper;
import com.phuong.mapper.ExpenseReportMapper;
import com.phuong.model.AppUser;
import com.phuong.model.Expense;
import com.phuong.repository.ExpenseRepository;
import com.phuong.service.AsyncExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncExpenseServiceImpl implements AsyncExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WebClient.Builder webClientBuilder;
    private final ExpenseMapper expenseMapper;
    private final ExpenseReportMapper reportMapper;

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional(readOnly = true)
    public CompletableFuture<List<ExpenseResponse>> getAllUserExpenseAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Fetching all expenses for user Id: {} on thread: {}", userId, Thread.currentThread().getName());
            try {
                List<Expense> expenses = expenseRepository
                        .findByUserIdOrderByDateDesc(userId);

                List<ExpenseResponse> expenseResponses = expenseMapper.toResponseList(expenses);

                log.info("Fetching {} expenses for user Id: {}", expenseResponses.size(), userId);
                return expenseResponses;

            } catch (DataAccessException ex) {
                log.error("Database error while fetching expenses for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to get expenses", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional(readOnly = true)
    public CompletableFuture<Optional<ExpenseResponse>> getExpenseByIdAsync(Long id, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Fetching expense for user Id: {} by Id: {} on thread: {}",
                    userId, id, Thread.currentThread().getName());

            if (id == null || id <= 0) {
                throw new RuntimeException("Expense Id must be positive number");
            }
            try {
                Optional<Expense> expense = expenseRepository.findByIdAndUserId(id, userId);

                if (expense.isPresent()) {
                    ExpenseResponse response = expenseMapper.toResponse(expense.get());
                    log.info("Found expense for user Id: {} by Id: {}", userId, id);
                    return Optional.of(response);
                } else {
                    log.warn("Expense not found for user Id: {} by Id: {}", userId, id);
                    return Optional.empty();
                }

            } catch (DataAccessException ex) {
                log.error("Database error while fetching expense for user Id: {} by Id: {}", userId, id, ex);
                throw new RuntimeException("Failed to get expense for user Id by Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional(readOnly = true)
    public CompletableFuture<List<ExpenseResponse>> getExpensesByDateRangeAsync(Long userId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Fetching expenses for user Id: {} between {} and {} on thread {}",
                    userId, startDate, endDate, Thread.currentThread().getName());
            try {
                List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

                List<ExpenseResponse> responses = expenseMapper.toResponseList(expenses);
                log.info("Getting {} expenses for user Id: {} in date range",
                        responses.size(), userId);
                return responses;

            } catch (DataAccessException ex) {
                log.error("Database error while getting expenses by date range for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to get expenses by date range", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional(readOnly = true)
    public CompletableFuture<ExpenseSummary> getExpenseSummaryAsync(Long userId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Generating expense summary for user Id: {} on thread: {}",
                    userId, Thread.currentThread().getName());
            try {
                List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

                ExpenseSummary summary = reportMapper.createSummary(expenses, userId, startDate, endDate);

                log.info("Generated expense summary for user Id: {} - Total: {}, count: {}",
                        userId, summary.getTotalAmount(), summary.getTotalTransactions());
                return summary;

            } catch (DataAccessException ex) {
                log.error("Database error while generating expense summary for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to generate expense summary", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional
    public CompletableFuture<ExpenseResponse> createExpenseAsync(ExpenseRequest request, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Creating new expense for User Id: {} on thread: {}", userId, Thread.currentThread().getName());


            try {
                CompletableFuture<AppUser> user = validateUserExistAsync(userId);
                AppUser userResult = user.join();
                if (user.join() != null) {
                    throw new ResourceNotFoundException("User", "Id", userId);
                }
                Expense expense = expenseMapper.toEntityWithUser(request, userResult);
                StringBuilder builder = validateExpense(expense, false);

                if (!builder.isEmpty()) {
                    throw new BusinessRuleException(builder.toString());
                }
                
                Expense savedExpense = expenseRepository.save(expense);

                CompletableFuture.allOf(
                        syncExpenseWithExternalSystemAsync(userId)
                ).exceptionally(ex -> {
                   log.warn("Some post-processing operations failed for expense creation", ex);
                   return null;
                });

                ExpenseResponse response = expenseMapper.toResponse(savedExpense);
                log.info("Created expense with Id: {} for User Id: {}",
                        savedExpense.getId(), userId);
                return response;

            } catch (DataAccessException ex) {
                log.error("Database error while creating expense for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to create expense for user Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional
    public CompletableFuture<ExpenseResponse> updateExpenseAsync(ExpenseRequest request, Long id, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Updating expense id: {} for User Id: {} on thread: {}", id, userId, Thread.currentThread().getName());

            try {
                Expense expense = expenseMapper.toEntity(request);
                expense.setId(id);
                StringBuilder builder = validateExpense(expense, true);

                if (!builder.isEmpty()) {
                    throw new BusinessRuleException(builder.toString());
                }

                Optional<Expense> existingExpense = expenseRepository.findByIdAndUserId(expense.getId(), userId);

                if (existingExpense.isEmpty()) {
                    log.warn("Not found expense for user Id: {} by Id: {}", userId, expense.getId());
                    throw new ResourceNotFoundException("Not found expense by user Id and Id");
                }

                expense.setUser(existingExpense.get().getUser());
                Expense savedExpense = expenseRepository.save(expense);

                ExpenseResponse response = expenseMapper.toResponse(savedExpense);

                log.info("Updated expense for User Id: {} by Id: {}", userId, savedExpense.getId());
                return response;

            } catch (DataAccessException ex) {
                log.error("Database error while updating expense Id: {} for user Id: {}", id, userId, ex);
                throw new RuntimeException("Failed to update expense for user Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional
    public CompletableFuture<Boolean> deleteExpenseAsync(Long id, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Deleting expense id: {} for User Id: {} on thread: {}", id, userId, Thread.currentThread().getName());

            try {
                Optional<Expense> existingExpense = expenseRepository.findByIdAndUserId(id, userId);

                if (existingExpense.isEmpty()) {
                    log.warn("Not found expense for user Id: {} by Id: {}", userId, id);
                    return false;
                }

                expenseRepository.deleteById(id);
                log.info("Deleted expense for User Id: {} by Id: {}", userId, id);
                return true;

            } catch (DataAccessException ex) {
                log.error("Database error while deleting expense Id: {} for user Id: {}", id, userId, ex);
                throw new RuntimeException("Failed to delete expense for user Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.TASK_EXECUTOR)
    @Transactional
    public CompletableFuture<List<ExpenseResponse>> createExpenseAsync(List<ExpenseRequest> requests, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Creating {} expenses asynchronously on thread: {}",
                    requests.size(), userId);
            try {
                CompletableFuture<AppUser> user = validateUserExistAsync(userId);
                AppUser userResult = user.join();
                if (user.join() != null) {
                    throw new ResourceNotFoundException("User", "Id", userId);
                }

                List<Expense> expenses = expenseMapper.toEntityListWithUser(requests, userResult);

                StringBuilder builder = new StringBuilder();
                AtomicInteger counter = new AtomicInteger(1);
                expenses.forEach((expense) -> {
                    StringBuilder temp = validateExpense(expense, false);
                    if (!temp.isEmpty()) {
                        builder.append(counter + " - " + temp.toString() + "\n");
                    }
                    counter.getAndIncrement();
                });

                if (!builder.isEmpty()) {
                    throw new BusinessRuleException(builder.toString());
                }

                List<Expense> savedExpenses = expenseRepository.saveAll(expenses);

                CompletableFuture.allOf(
                        syncExpenseWithExternalSystemAsync(userId)
                ).exceptionally(ex -> {
                    log.warn("Some post-processing operations failed for expense creation", ex);
                    return null;
                });

                List<ExpenseResponse> responses = expenseMapper.toResponseList(savedExpenses);

                log.info("Created {} expenses for User Id: {}",
                        savedExpenses.size(), userId);
                return responses;
                
            } catch (DataAccessException ex) {
                log.error("Database error while creating expenses for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to create expenses for user Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.EXTERNAL_TASK_EXECUTOR)
    public CompletableFuture<AppUser> validateUserExistAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Validating user exists: {} on thread: {}",
                    userId, Thread.currentThread().getName());
            try {
                AppUser user = webClientBuilder.build().get()
                        .uri("http://user-service/user/" + userId.toString())
                        .retrieve()
                        .bodyToMono(AppUser.class)
                        .timeout(Duration.ofSeconds(10))
                        .block();
                return user;

            } catch (WebClientResponseException ex) {
                log.error("User service returned error for user Id: {} - Status: {} - Response: {}",
                        userId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
                if (ex.getStatusCode().is4xxClientError()) {
                    return null;
                }
                throw new ExternalServiceException("user-service",
                        "Failed to get user: " + ex.getMessage(), ex);
            } catch (WebClientException ex) {
                log.error("Failed to communicate with user service by user Id: {}", userId, ex);
                throw new ExternalServiceException("user-service",
                        "Communication error: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                log.error("Unexpected error while getting user with user Id: {}", userId, ex);
                throw new ExternalServiceException("user-service",
                        "Unexpected error: " + ex.getMessage(), ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.HEAVY_TASK_EXECUTOR)
    @Transactional(readOnly = true)
    public CompletableFuture<ExpenseReport> generateMonthlyReportAsync(Long userId, int month, int year) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Generating monthly report for user: {} for {}/{} on thread: {}",
                    userId, month, year, Thread.currentThread().getName());

            try {
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.plusMonths(1).minusDays(1);

                List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
                );

                ExpenseReport report = reportMapper.createReport(expenses, startDate, endDate, "Monthly Report");

                log.info("Generating monthly report for user: {} - {} expenses processed",
                        userId, expenses.size());
                return report;

            } catch (DataAccessException ex) {
                log.error("Database error while generating monthly report for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to generate monthly report for user Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.HEAVY_TASK_EXECUTOR)
    @Transactional(readOnly = true)
    public CompletableFuture<ExpenseReport> generateCategoryReportAsync(Long userId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Generating category report for user: {} between {} and {} on thread: {}",
                    userId, startDate, endDate, Thread.currentThread().getName());

            try {

                List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
                );

                ExpenseReport report = reportMapper.createReport(expenses, startDate, endDate, "Category Report");

                log.info("Generating category report for user: {} - {} expenses processed",
                        userId, expenses.size());
                return report;

            } catch (DataAccessException ex) {
                log.error("Database error while generating category report for user Id: {}", userId, ex);
                throw new RuntimeException("Failed to generate category report for user Id", ex);
            }
        });
    }

    @Override
    @Async(ExecutorConstants.EXTERNAL_TASK_EXECUTOR)
    public CompletableFuture<Void> syncExpenseWithExternalSystemAsync(Long userId) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Syncing expenses with external systems for user: {} on thread: {}",
                    userId, Thread.currentThread().getName());

            try {
                Thread.sleep(200);
                log.debug("Expenses synced with external systems for user: {}", userId);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Expenses synced with external systems for user: {}", userId, ex);
                throw new ExternalServiceException("user-service",
                        "Unexpected error: " + ex.getMessage(), ex);
            }
            catch (Exception ex) {
                log.error("Unexpected error while getting user with user Id: {}", userId, ex);
                throw new ExternalServiceException("user-service",
                        "Unexpected error: " + ex.getMessage(), ex);
            }
        });
    }

    private StringBuilder validateExpense(Expense expense, boolean isExisted) {
        StringBuilder builder = new StringBuilder();
        
        if (expense == null) {
            builder.append("Expense cannot be null");
        } else {
            if (isExisted && (expense.getId() == null || expense.getId() <= 0)) {
                builder.append("Expense Id must be provided");
            }
            if (expense.getAmount() <= 0) {
                builder.append("Expense amount must be positive");
            }

            if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
                builder.append("Expense category is required");
            }

            if (expense.getDate() == null) {
                builder.append("Expense date is required");
            }

            if (expense.getAccount() == null || expense.getAccount().trim().isEmpty()) {
                builder.append("Expense account is required");
            }
        }
        return builder;
    }
}
