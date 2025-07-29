package com.phuong.mapper;

import com.phuong.dto.request.ExpenseRequest;
import com.phuong.dto.response.ExpenseReport;
import com.phuong.dto.response.ExpenseSummary;
import com.phuong.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {ExpenseMapper.class}, // Use ExpenseMapper for nested mappings
        imports = {LocalDateTime.class, BigDecimal.class}
)
public interface ExpenseReportMapper {
    /**
     * Create an ExpenseReport from the list of expenses
     * @param expenses the list of expenses
     * @param startDate the report start date
     * @param endDate the report end date
     * @param reportType the type of report
     * @return the expense report
     */
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "reportType", source = "reportType")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(expenses))")
    @Mapping(target = "totalTransactions", expression = "java(expenses.size())")
    @Mapping(target = "expenses", source = "expenses")
    @Mapping(target = "generatedAt", expression = "java(LocalDateTime.now())")
    ExpenseReport createReport(
            List<Expense> expenses,
            LocalDate startDate,
            LocalDate endDate,
            String reportType);

    /**
     * Create an ExpenseSummary from the list of expenses
     * @param expenses the list of expenses
     * @param userId the user Id
     * @param startDate the summary start date
     * @param endDate the summary end date
     * @return the expense summary
     */
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(expenses))")
    @Mapping(target = "totalTransactions", expression = "java(expenses.size())")
    @Mapping(target = "averageAmount", expression = "java(calculateAverageAmount(expenses))")
    @Mapping(target = "generatedAt", expression = "java(LocalDateTime.now())")
    ExpenseSummary createSummary(
            List<Expense> expenses,
            Long userId,
            LocalDate startDate,
            LocalDate endDate);

    default double calculateTotalAmount(List<Expense> expenses) {
        ExpenseReport expenseReport = new ExpenseReport();
        return expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    default BigDecimal calculateAverageAmount(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.valueOf(calculateTotalAmount(expenses));
        return total.divide(BigDecimal.valueOf(expenses.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
}
