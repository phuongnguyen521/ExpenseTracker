package com.phuong.repository;

import com.phuong.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId ORDER BY e.date DESC")
    List<Expense> findByUserIdOrderByDateDesc(@Param("userId") Long userId);

    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.user.id = :userId")
    Optional<Expense> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC")
    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}