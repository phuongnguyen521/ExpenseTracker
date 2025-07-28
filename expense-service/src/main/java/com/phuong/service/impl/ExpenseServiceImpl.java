package com.phuong.service.impl;

import com.phuong.model.AppUser;
import com.phuong.model.Expense;
import com.phuong.repository.ExpenseRepository;
import com.phuong.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public List<Expense> getAllUserExpenses(Long userId) {
        return new ArrayList<>(expenseRepository
                .findByUserIdOrderByDateDesc(userId));
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return Optional.ofNullable(webClientBuilder.build().get()
                .uri("http://user-service/user/username/" + username)
                .retrieve()
                .bodyToMono(AppUser.class)
                .block());
    }

    @Override
    public List<Expense> getExpenseByDay(String date, Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .filter(expense -> expense.getDate().equals(date))
                .toList();
    }

    @Override
    public List<Expense> getExpenseByCategoryAndMonth(String category, String month, Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .filter(expense -> expense.getCategory().equalsIgnoreCase(category)
                        && expense.getDate().startsWith(month))
                .toList();
    }

    @Override
    public List<String> getAllExpenseCategories(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(Expense::getCategory)
                .distinct()
                .toList();
    }

    @Override
    public Optional<Expense> getExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public Expense addExpense(Expense expense, Long userId) {
        // Get user
        Optional<AppUser> user = Optional.ofNullable(webClientBuilder.build().get()
                .uri("http://user-service/user/" + userId.toString())
                .retrieve()
                .bodyToMono(AppUser.class)
                .block());
//        Optional<AppUser> user = userService.findUserById(userId);
        if (user.isPresent()) {
            AppUser updateUser = user.get();
            expense.setUser(updateUser);
            return expenseRepository.save(expense);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    public boolean updateExpense(Expense expense, Long userId) {
        Optional<Expense> existingExpense = expenseRepository
                .findByIdAndUserId(expense.getId(), userId);
        if (existingExpense.isPresent()) {
            expense.setUser(existingExpense.get().getUser());
            expenseRepository.save(expense);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteExpense(Long id, Long userId) {
        Optional<Expense> existingExpense = expenseRepository
                .findByIdAndUserId(id, userId);
        if (existingExpense.isPresent()) {
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
