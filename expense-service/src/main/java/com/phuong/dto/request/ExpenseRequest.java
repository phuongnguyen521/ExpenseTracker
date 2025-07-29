package com.phuong.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {
    private Long userId;

    @NotNull(message = "Type is required")
    private int expenseType;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0", message = "Amount is not negative")
    private double amount;

    @NotNull(message = "Category is required")
    private String category;

    @NotNull(message = "Account is required")
    private String account;

    private String note;
}
