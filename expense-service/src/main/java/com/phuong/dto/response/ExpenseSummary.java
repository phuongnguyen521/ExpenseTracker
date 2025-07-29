package com.phuong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummary {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalAmount;
    private int totalTransactions;
    private BigDecimal averageAmount;
    private LocalDateTime generatedAt;
}
