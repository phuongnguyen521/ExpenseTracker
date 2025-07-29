package com.phuong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseReport {
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalAmount;
    private int totalTransactions;
    private List<ExpenseResponse> expenses;
    private LocalDateTime generatedAt;
}
