package com.phuong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private Long userId;
    private int expenseType;
    private LocalDate date;
    private double amount;
    private String category;
    private String account;
    private String note;
}
