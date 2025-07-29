package com.phuong.service.impl;

import com.phuong.exception.BusinessRuleException;
import com.phuong.exception.ExternalServiceException;
import com.phuong.exception.ResourceNotFoundException;
import com.phuong.model.AppUser;
import com.phuong.model.Expense;
import com.phuong.repository.ExpenseRepository;
import com.phuong.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getAllUserExpenses(Long userId) {
        log.debug("Fetching all expenses for user Id: {}", userId);
        try {
            List<Expense> expenses = expenseRepository
                    .findByUserIdOrderByDateDesc(userId);
            log.info("Fetching {} expenses for user Id: {}", expenses.size(), userId);
            return expenses;
        } catch (DataAccessException ex) {
            log.error("Database error while fetching expenses for user Id: {}", userId, ex);
            throw new RuntimeException("Failed to get expenses", ex);
        }
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        log.debug("Get user by username: {}", username);
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessRuleException("Username cannot be null or empty");
        }

        try {
            AppUser user = webClientBuilder.build().get()
                    .uri("http://user-service/user/username/" + username)
                    .retrieve()
                    .bodyToMono(AppUser.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            if (user != null) {
                log.info("Found user with username: {}", username);
                return Optional.of(user);
            } else {
                log.warn("User not found with username: {}", username);
                return Optional.empty();
            }
        } catch (WebClientResponseException ex) {
            log.error("User service returned error for username: {} - Status: {}, Response: {}",
                    username, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);

            if (ex.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            throw new ExternalServiceException("user-service",
                    "Failed to get user: " + ex.getMessage(), ex);
        } catch (WebClientException ex) {
            log.error("Failed to communicate with user service by username: {}", username, ex);
            throw new ExternalServiceException("user-service",
                    "Communication error: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error while getting user with username: {}", username, ex);
            throw new ExternalServiceException("user-service",
                    "Unexpected error: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpenseByDay(String date, Long userId) {
        log.debug("Fetching expenses for user Id: {} on date: {}", userId, date);
        if (date == null || date.trim().isEmpty()) {
            throw new BusinessRuleException("Date cannot be null or empty");
        }
        try {
            List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(userId)
                    .stream()
                    .filter(expense -> expense.getDate().equals(date))
                    .toList();
            log.info("Found {} expenses for user Id: {} on date: {}", expenses.size(), userId, date);
            return expenses;
        } catch (DataAccessException ex) {
            log.error("Database error while fetching expenses for user Id: {} on date: {}", userId, date, ex);
            throw new RuntimeException("Failed to get expenses by date", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpenseByCategoryAndMonth(String category, String month, Long userId) {
        log.debug("Fetching expenses for user Id: {} in category: {} for month: {}", userId, category, month);

        if (month == null || month.trim().isEmpty()) {
            throw new BusinessRuleException("Month is invalid");
        }

        if (category == null || category.trim().isEmpty()) {
            throw new BusinessRuleException("Category cannot be null or empty");
        }

        try {
            List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(userId)
                    .stream()
                    .filter(expense -> expense.getCategory().equalsIgnoreCase(category)
                            && String.valueOf(expense.getDate().getMonthValue()).equals(month))
                    .toList();
            log.info("Found {} expenses for user Id: {} in category: {} for month: {}",
                    expenses.size(), userId, category, month);
            return expenses;
        } catch (DataAccessException ex) {
            log.error("Database error while fetching expenses for user Id: {} in category: {} for month: {}", userId, category, month, ex);
            throw new RuntimeException("Failed to get expenses by category and month", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllExpenseCategories(Long userId) {
        log.debug("Fetching categories for user Id: {}", userId);
        try {
            List<String> categories = expenseRepository.findByUserIdOrderByDateDesc(userId)
                    .stream()
                    .map(Expense::getCategory)
                    .distinct()
                    .toList();
            log.info("Found {} categories for user Id: {}",
                    categories.size(), userId);
            return categories;
        } catch (DataAccessException ex) {
            log.error("Database error while fetching categories for user Id: {}", userId, ex);
            throw new RuntimeException("Failed to get categories for user Id", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Expense> getExpenseById(Long id, Long userId) {
        log.debug("Fetching expense for user Id: {} by Id: {}", userId, id);

        if (id == null || id <= 0) {
            throw new RuntimeException("Expense Id must be positive number");
        }
        try {
            Optional<Expense> expense = expenseRepository.findByIdAndUserId(id, userId);

            if (expense.isPresent()) {
                log.info("Found expense for user Id: {} by Id: {}", userId, id);
            } else {
                log.warn("Expense not found for user Id: {} by Id: {}", userId, id);
            }
            return expense;

        } catch (DataAccessException ex) {
            log.error("Database error while fetching expense for user Id: {} by Id: {}", userId, id, ex);
            throw new RuntimeException("Failed to get expense for user Id by Id", ex);
        }
    }

    @Override
    @Transactional
    public Expense addExpense(Expense expense, Long userId) {
        log.debug("Adding new expense for User Id: {}", userId);

        validateExpense(expense);

        try {
            Optional<AppUser> userOpt = getUserById(userId);
            if (userOpt.isEmpty()) {
                throw new ResourceNotFoundException("User", "Id", userId);
            }
            AppUser user = userOpt.get();
            expense.setUser(user);
            Expense savedExpense = expenseRepository.save(expense);
            log.info("Added expense with Id: {} for User Id: {}",
                    savedExpense.getId(), userId);
            return savedExpense;
        } catch (DataAccessException ex) {
            log.error("Database error while adding expense for user Id: {}", userId, ex);
            throw new RuntimeException("Failed to add expense for user Id", ex);
        }
    }

    @Override
    @Transactional
    public boolean updateExpense(Expense expense, Long userId) {
        log.debug("Updating expense for User Id: {} by Id: {}", userId, expense.getId());

        validateExpense(expense);

        if (expense.getId() == null || expense.getId() <= 0) {
            throw new BusinessRuleException("Expense Id must be provided");
        }

        try {
            Optional<Expense> existingExpense = expenseRepository.findByIdAndUserId(expense.getId(), userId);

            if (existingExpense.isEmpty()) {
                log.warn("Not found expense for user Id: {} by Id: {}", userId, expense.getId());
                return false;
            }

            expense.setUser(existingExpense.get().getUser());
            expenseRepository.save(expense);

            log.info("Updated expense for User Id: {} by Id: {}", userId, expense.getId());
            return true;

        } catch (DataAccessException ex) {
            log.error("Database error while updating expense for user Id: {} by Id: {}",
                    userId, expense.getId(), ex);
            throw new RuntimeException("Failed to update expense for user Id by Id", ex);
        }
    }

    @Override
    @Transactional
    public boolean deleteExpense(Long id, Long userId) {
        log.debug("Deleting expense for User Id: {} by Id: {}", userId, id);

        if (id == null || id <= 0) {
            throw new BusinessRuleException("Expense Id must be positive");
        }

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
            log.error("Database error while deleting expense for user Id: {} by Id: {}",
                    userId, id, ex);
            throw new RuntimeException("Failed to delete expense for user Id by Id", ex);
        }
    }

    private Optional<AppUser> getUserById(Long userId) {
        try {
            AppUser user = webClientBuilder.build().get()
                    .uri("http://user-service/user/" + userId.toString())
                    .retrieve()
                    .bodyToMono(AppUser.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return Optional.ofNullable(user);

        } catch (WebClientResponseException ex) {
            log.error("User service returned error for user Id: {} - Status: {} - Response: {}",
                    userId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            if (ex.getStatusCode().is4xxClientError()) {
                return Optional.empty();
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
    }

    private void validateExpense(Expense expense) {
        if (expense == null) {
            throw new BusinessRuleException("Expense cannot be null");
        }

        if (expense.getAmount() <= 0) {
            throw new BusinessRuleException("Expense amount must be positive");
        }

        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new BusinessRuleException("Expense category is required");
        }

        if (expense.getDate() == null) {
            throw new BusinessRuleException("Expense date is required");
        }

        if (expense.getAccount() == null || expense.getAccount().trim().isEmpty()) {
            throw new BusinessRuleException("Expense account is required");
        }
    }
}
