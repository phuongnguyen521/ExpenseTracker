package org.example.service.impl;

import org.example.model.AppUser;
import org.example.model.Expense;
import org.example.repository.ExpenseRepository;
import org.example.service.ExpenseService;
import org.example.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Override
    public List<Expense> getAllUserExpenses(Long userId) {
        return new ArrayList<>(expenseRepository
                .findByUserIdOrderByDateDesc(userId));
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
        Optional<AppUser> user = userService.findUserById(userId);
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


    // Get data from json file
    /*
    private static final AtomicLong idCounter = new AtomicLong();

    @Override
    public List<Expense> getExpenseByDay(String date) {
        return ExpenseDataLoader
                .getExpenses()
                .stream()
                .filter(expense -> expense.getDate().equalsIgnoreCase(date))
                .toList();
    }

    @Override
    public List<Expense> getExpenseByCategoryAndMonth(String category, String month) {
        return ExpenseDataLoader
                .getExpenses()
                .stream()
                .filter(expense -> expense.getCategory().equalsIgnoreCase(category) &&
                        expense.getDate().startsWith(month))
                .toList();
    }

    @Override
    public List<String> getAllExpenseCategories() {
        return ExpenseDataLoader
                .getExpenses()
                .stream()
                .map(Expense::getCategory)
                .distinct()
                .toList();
    }

    @Override
    public Optional<Expense> getExpenseById(Long id) {
        return ExpenseDataLoader
                .getExpenses()
                .stream()
                .filter(expense -> expense.getId().equals(id))
                .findFirst();
    }

    @Override
    public Expense addExpense(Expense expense) {
        expense.setId(idCounter.incrementAndGet());
        ExpenseDataLoader.getExpenses().add(expense);
        return expense;
    }

    @Override
    public boolean updateExpense(Expense expense) {
        Optional<Expense> existingExpense = getExpenseById(expense.getId());
        if (existingExpense.isPresent()) {
            ExpenseDataLoader.getExpenses().remove(existingExpense.get());
            ExpenseDataLoader.getExpenses().add(expense);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteExpense(Long id) {
        Optional<Expense> existingExpense = getExpenseById(id);
        if (existingExpense.isPresent()) {
            ExpenseDataLoader.getExpenses().remove(existingExpense.get());
            return true;
        }
        return false;
    }*/
}
