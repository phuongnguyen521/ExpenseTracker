package com.phuong.controller;

import com.phuong.dto.request.ExpenseRequest;
import com.phuong.dto.response.ExpenseReport;
import com.phuong.dto.response.ExpenseResponse;
import com.phuong.dto.response.ExpenseSummary;
import com.phuong.exception.ResourceNotFoundException;
import com.phuong.service.AsyncExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class AsyncExpenseController {
    private final AsyncExpenseService expenseService;

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<List<ExpenseResponse>>> getUserExpenses(@PathVariable Long userId) {
        log.info("Async request: Get all expenses for user: {}", userId);

        return expenseService.getAllUserExpenseAsync(userId)
                .thenApply(expenses -> {
                    log.info("Async response: Get {} expenses for user: {}", expenses.size(), userId);
                    return ResponseEntity.ok(expenses);
                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {}", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}/range")
    public CompletableFuture<ResponseEntity<List<ExpenseResponse>>> getUserExpensesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Async request: Get expenses for user: {} between: {} and {}", userId, startDate, endDate);

        return expenseService.getExpensesByDateRangeAsync(userId, startDate, endDate)
                .thenApply(expenses -> {
                    log.info("Async response: Get {} expenses for user: {} by date range", expenses.size(), userId);
                    return ResponseEntity.ok(expenses);
                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {} by date range", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}/expense/{id}")
    public CompletableFuture<ResponseEntity<ExpenseResponse>> getExpenseById(
            @PathVariable Long userId,
            @PathVariable Long id) {
        log.info("Async request: Get expenses for user: {} by Id: {}", userId, id);

        return expenseService.getExpenseByIdAsync(id, userId)
                .thenApply(expense -> {

                    if (expense.isEmpty()) {
                        log.info("Error get expense for user: {} by Id: {}", userId, id);
                        throw new ResourceNotFoundException("Expense", "Id", id);
                    }
                    log.info("Async response: Get expense for user: {} by Id: {}", userId, id);
                    return ResponseEntity.ok(expense.get());

                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {} by Id: {}", userId, id, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}/summary")
    public CompletableFuture<ResponseEntity<ExpenseSummary>> getExpenseSummary(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Async request: Generating summary expense for user: {}", userId);

        return expenseService.getExpenseSummaryAsync(userId, startDate, endDate)
                .thenApply(summary -> {
                    log.info("Async response: Generated summary expense for user: {}", userId);
                    return ResponseEntity.ok(summary);
                }).exceptionally(ex -> {
                    log.error("Error generating summary expense by user: {}", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}/summary")
    public CompletableFuture<ResponseEntity<ExpenseReport>> generateMonthlyReport(
            @PathVariable Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("Async request: Generating monthly report for user: {} for {}/{}", userId, month, year);

        return expenseService.generateMonthlyReportAsync(userId, month, year)
                .thenApply(report -> {
                    log.info("Async response: Generated monthly report for user: {}", userId);
                    return ResponseEntity.ok(report);
                }).exceptionally(ex -> {
                    log.error("Error generating monthly report by user: {}", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}/summary")
    public CompletableFuture<ResponseEntity<ExpenseReport>> generateCategoryReport(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Async request: Generating category report for user: {}", userId);

        return expenseService.generateCategoryReportAsync(userId, startDate, endDate)
                .thenApply(report -> {
                    log.info("Async response: Generated category report for user: {}", userId);
                    return ResponseEntity.ok(report);
                }).exceptionally(ex -> {
                    log.error("Error generating category report by user: {}", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PostMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<ExpenseResponse>> createExpense(
            @PathVariable Long userId,
            @Valid @RequestBody ExpenseRequest request) {
        log.info("Async request: Create expense for user: {}", userId);

        return expenseService.createExpenseAsync(request, userId)
                .thenApply(expense -> {
                    log.info("Async response: Created expense for user: {} with Id", userId, expense.getId());
                    return ResponseEntity.ok(expense);
                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {}", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    /*
     * curl -X POST "http://localhost:8080/api/v1/expenses/bulk" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '[
    {
      "userId": 123,
      "amount": 45.50,
      "description": "Lunch at restaurant",
      "category": "FOOD",
      "date": "2025-07-29"
    },
    {
      "userId": 123,
      "amount": 25.00,
      "description": "Gas for car",
      "category": "TRANSPORTATION",
      "date": "2025-07-29"
    },
    {
      "userId": 123,
      "amount": 120.75,
      "description": "Groceries",
      "category": "FOOD",
      "date": "2025-07-28"
    }
  ]'
    */
    @PostMapping("/user/{userId}/bulk")
    public CompletableFuture<ResponseEntity<List<ExpenseResponse>>> createExpenses(
            @PathVariable Long userId,
            @Valid @RequestBody List<ExpenseRequest> requests) {
        log.info("Async request: Create {} expenses for user: {}", requests.size(), userId);

        return expenseService.createExpenseAsync(requests, userId)
                .thenApply(expenses -> {
                    log.info("Async response: Created {} expenses for user: {}", expenses.size(), userId);
                    return ResponseEntity.ok(expenses);
                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {}", userId, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PutMapping("/user/{userId}/expense/{id}")
    public CompletableFuture<ResponseEntity<ExpenseResponse>> updateExpense(
            @PathVariable Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        log.info("Async request: Update expense for user: {} by Id: {}", userId, id);

        return expenseService.updateExpenseAsync(request, id, userId)
                .thenApply(expense -> {
                    log.info("Async response: Update expense for user: {} by Id: {}", userId, id);
                    return ResponseEntity.ok(expense);
                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {} by Id: {}", userId, id, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PostMapping("/user/{userId}/expense/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteExpense(
            @PathVariable Long userId,
            @PathVariable Long id) {
        log.info("Async request: Update expense for user: {} by Id: {}", userId, id);

        return expenseService.deleteExpenseAsync(id, userId)
                .thenApply(isDeleted -> {
                    if (!isDeleted) {
                        log.warn("Expense not found for deletion - Id: {} for user: {}", id, userId);
                        throw new ResourceNotFoundException("Expense", "id", id);
                    }

                    log.info("Async response: Update expense for user: {} by Id: {}", userId, id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

                }).exceptionally(ex -> {
                    log.error("Error getting expenses by user: {} by Id: {}", userId, id, ex);
                    return ResponseEntity.internalServerError().build();
                });
    }

}
