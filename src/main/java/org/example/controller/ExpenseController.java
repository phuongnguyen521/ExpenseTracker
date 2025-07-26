package org.example.controller;

import org.example.model.AppUser;
import org.example.model.Expense;
import org.example.service.ExpenseService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping("/expenses/categories")
    public ResponseEntity<List<String>> getAllExpenseCategories(Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        List<String> categories = expenseService.getAllExpenseCategories(user.getId());

        if (categories.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(null);
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/expenses/day/{date}")
    public ResponseEntity<List<Expense>> getExpenseByDay(@PathVariable String date, Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        List<Expense> expenses = expenseService.getExpenseByDay(date, user.getId());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/category/{category}/month")
    public ResponseEntity<List<Expense>> getExpenseByCategoryAndMonth(
            @PathVariable String category,
            @RequestParam String month,
            Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        List<Expense> expenses = expenseService.getExpenseByCategoryAndMonth(category, month, user.getId());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<Optional<Expense>> getExpenseById(
            @PathVariable Long id,
            Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        return ResponseEntity.ok(expenseService.getExpenseById(id, user.getId()));
    }

    @PostMapping("/expenses")
    public ResponseEntity<Expense> addExpense(
            @RequestBody Expense expense,
            Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        Expense newExpense = expenseService.addExpense(expense, user.getId());
        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @RequestBody Expense expense,
            Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        expense.setId(id);
        boolean isUpdated = expenseService.updateExpense(expense, user.getId());
        if (isUpdated) {
            return new ResponseEntity<>(expense, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> updateExpense(
            @PathVariable Long id,
            Authentication authentication) {
        AppUser user = getUserFromAuthentication(authentication);
        boolean isDelete = expenseService.deleteExpense(id, user.getId());
        if (isDelete) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private AppUser getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}
