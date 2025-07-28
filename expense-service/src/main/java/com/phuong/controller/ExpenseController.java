package com.phuong.controller;

import com.phuong.exception.ResourceNotFoundException;
import com.phuong.model.AppUser;
import com.phuong.model.Expense;
import com.phuong.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/categories/{username}")
    public ResponseEntity<List<String>> getAllExpenseCategories(@PathVariable String username) {
        log.info("Received request to get categories for username: {}", username);

        AppUser user = getUserFromAuthentication(username);
        List<String> categories = expenseService.getAllExpenseCategories(user.getId());

        if (categories.isEmpty()) {
            log.info("No categories for username: {}", username);
            return ResponseEntity.noContent().build();
        }
        log.info("Getting {} categories for username: {}", categories.size(), username);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/day/{date}/{username}")
    public ResponseEntity<List<Expense>> getExpenseByDay(@PathVariable String date,
                                                         @PathVariable String username) {
        log.info("Received request to get expenses for username: {} by date: {}", username, date);
        AppUser user = getUserFromAuthentication(username);

        List<Expense> expenses = expenseService.getExpenseByDay(date, user.getId());
        log.info("Getting {} expenses for username: {} by date: {}",
                expenses.size(), username, date);

        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/category/{category}/month/{username}")
    public ResponseEntity<List<Expense>> getExpenseByCategoryAndMonth(
            @PathVariable String category,
            @RequestParam String month,
            @PathVariable String username) {
        log.info("Received request to get expenses for username: {} in category: {} by month: {}",
                username, category, month);
        AppUser user = getUserFromAuthentication(username);

        List<Expense> expenses = expenseService.getExpenseByCategoryAndMonth(category, month, user.getId());
        log.info("Getting {} expenses for username: {} in category: {} by month: {}",
                expenses.size(), username, category, month);

        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}/{username}")
    public ResponseEntity<Optional<Expense>> getExpenseById(
            @PathVariable Long id,
            @PathVariable String username) {
        log.info("Received request to get expense for username: {} by Id: {}", username, id);
        AppUser user = getUserFromAuthentication(username);
        Optional<Expense> expense = expenseService.getExpenseById(id, user.getId());

        if (expense.isEmpty()) {
            throw new ResourceNotFoundException("Expense", "id", id);
        }

        log.info("Getting expense for username: {} by Id: {}", username, id);
        return ResponseEntity.ok(expense);
    }

    @PostMapping("/{username}")
    public ResponseEntity<Expense> addExpense(
            @Valid @RequestBody Expense expense,
            @PathVariable String username) {
        log.info("Received request to add expense for username: {}", username);
        AppUser user = getUserFromAuthentication(username);
        Expense newExpense = expenseService.addExpense(expense, user.getId());

        log.info("Adding expense for username: {} with Id: {}",
                username, newExpense.getId());
        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/{username}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody Expense expense,
            @PathVariable String username) {
        log.info("Received request to update expense for username: {} with Id: {}", username, id);
        AppUser user = getUserFromAuthentication(username);
        expense.setId(id);
        boolean isUpdated = expenseService.updateExpense(expense, user.getId());

        if (!isUpdated) {
            throw new ResourceNotFoundException("Expense", "id", id);
        }

        log.info("Updating expense for username: {} with Id: {}", username, id);
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{id}/{username}")
    public ResponseEntity<Void> updateExpense(
            @PathVariable Long id,
            @PathVariable String username) {
        log.info("Received request to delete expense for username: {} with Id: {}", username, id);
        AppUser user = getUserFromAuthentication(username);
        boolean isDelete = expenseService.deleteExpense(id, user.getId());

        if (!isDelete) {
            throw new ResourceNotFoundException("Expense", "id", id);
        }

        log.info("delete expense for username: {} with Id: {}", username, id);
        return ResponseEntity.noContent().build();
    }

    private AppUser getUserFromAuthentication(String username) {
        log.debug("Getting user by username: {}", username);
        Optional<AppUser> userOpt = expenseService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        return userOpt.get();
    }
}
