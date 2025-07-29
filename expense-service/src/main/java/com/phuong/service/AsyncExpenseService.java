package com.phuong.service;

import com.phuong.dto.request.ExpenseRequest;
import com.phuong.dto.response.ExpenseReport;
import com.phuong.dto.response.ExpenseResponse;
import com.phuong.dto.response.ExpenseSummary;
import com.phuong.model.AppUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AsyncExpenseService {

    // Async read operation
    CompletableFuture<List<ExpenseResponse>> getAllUserExpenseAsync(Long userId);

    CompletableFuture<Optional<ExpenseResponse>> getExpenseByIdAsync(Long id, Long userId);

    CompletableFuture<List<ExpenseResponse>> getExpensesByDateRangeAsync(
            Long userId, LocalDate startDate, LocalDate endDate);

    CompletableFuture<ExpenseSummary> getExpenseSummaryAsync(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Async write operation
    CompletableFuture<ExpenseResponse> createExpenseAsync(ExpenseRequest request, Long userId);

    CompletableFuture<ExpenseResponse> updateExpenseAsync(ExpenseRequest request, Long id, Long userId);

    CompletableFuture<Boolean> deleteExpenseAsync(Long id, Long userId);

    CompletableFuture<List<ExpenseResponse>> createExpenseAsync(List<ExpenseRequest> requests, Long userId);

    CompletableFuture<AppUser> validateUserExistAsync(Long userId);

    // Async Reporting operation
    CompletableFuture<ExpenseReport> generateMonthlyReportAsync(Long userId, int month, int year);

    CompletableFuture<ExpenseReport> generateCategoryReportAsync(
            Long userId, LocalDate startDate, LocalDate endDate);


    // Async batch operation
    CompletableFuture<Void> syncExpenseWithExternalSystemAsync(Long userId);
}
