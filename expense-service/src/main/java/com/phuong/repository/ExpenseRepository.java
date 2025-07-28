package com.phuong.repository;

import com.phuong.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByDateDesc(Long userId);
    Optional<Expense> findByIdAndUserId(Long id, Long userId);
}