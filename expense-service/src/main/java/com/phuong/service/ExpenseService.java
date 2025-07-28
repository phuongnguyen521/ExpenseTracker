package com.phuong.service;

import com.phuong.model.AppUser;
import com.phuong.model.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseService {

    List<Expense> getAllUserExpenses(Long userId);

    Optional<AppUser> findByUsername(String username);
    List<Expense> getExpenseByDay(String date, Long userId);

    List<Expense> getExpenseByCategoryAndMonth(String category, String month, Long userId);

    List<String> getAllExpenseCategories(Long userId);

    Optional<Expense> getExpenseById(Long id, Long userId);

    Expense addExpense(Expense expense, Long userId);

    boolean updateExpense(Expense expense, Long userId);

    boolean deleteExpense(Long id, Long userId);
}
