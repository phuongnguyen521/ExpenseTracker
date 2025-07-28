package com.phuong.controller;

import com.phuong.model.AppUser;
import com.phuong.model.Expense;
import com.phuong.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/expenses/categories/{username}")
    public ResponseEntity<List<String>> getAllExpenseCategories(@PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);
        List<String> categories = new ArrayList<>();

        if (user.isPresent()) {
            categories = expenseService.getAllExpenseCategories(user.get().getId());
        }

        if (categories.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(null);
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/expenses/day/{date}/{username}")
    public ResponseEntity<List<Expense>> getExpenseByDay(@PathVariable String date,
                                                         @PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);

        List<Expense> expenses = new ArrayList<>();
        if (user.isPresent()) {
            expenses = expenseService.getExpenseByDay(date, user.get().getId());
        }
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/category/{category}/month/{username}")
    public ResponseEntity<List<Expense>> getExpenseByCategoryAndMonth(
            @PathVariable String category,
            @RequestParam String month,
            @PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);
        List<Expense> expenses = new ArrayList<>();
        if (user.isPresent()) {
            expenses = expenseService.getExpenseByCategoryAndMonth(category, month, user.get().getId());
        }
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/{id}/{username}")
    public ResponseEntity<Optional<Expense>> getExpenseById(
            @PathVariable Long id,
            @PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);
        Optional<Expense> expense = Optional.empty();
        if (user.isPresent()) {
            expense = expenseService.getExpenseById(id, user.get().getId());
        }
        return ResponseEntity.ok(expense);
    }

    @PostMapping("/expenses/{username}")
    public ResponseEntity<Expense> addExpense(
            @RequestBody Expense expense,
            @PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);
        Expense newExpense = null;
        if (user.isPresent()) {
            newExpense = expenseService.addExpense(expense, user.get().getId());
        }
        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    @PutMapping("/expenses/{id}/{username}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @RequestBody Expense expense,
            @PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);
        boolean isUpdated = false;
        if (user.isPresent()) {
            expense.setId(id);
            isUpdated = expenseService.updateExpense(expense, user.get().getId());
        }

        if (isUpdated) {
            return new ResponseEntity<>(expense, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/expenses/{id}/{username}")
    public ResponseEntity<Void> updateExpense(
            @PathVariable Long id,
            @PathVariable String username) {
        Optional<AppUser> user = getUserFromAuthentication(username);
        boolean isDelete = false;
        if (user.isPresent()) {
            isDelete = expenseService.deleteExpense(id, user.get().getId());
        }

        if (isDelete) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Optional<AppUser> getUserFromAuthentication(String username) {
        return expenseService.findByUsername(username);
    }
}
