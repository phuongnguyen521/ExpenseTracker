package com.phuong.mapper;

import com.phuong.dto.request.ExpenseRequest;
import com.phuong.dto.response.ExpenseResponse;
import com.phuong.model.AppUser;
import com.phuong.model.Expense;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ExpenseMapper {
    /**
     * Maps ExpenseRequest to Expense entity
     *
     * @param request the expense request
     * @return the expense entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseType", constant = "1")
    // Default expense type
    Expense toEntity(ExpenseRequest request);

    /**
     * Maps ExpenseRequest to Expense entity
     *
     * @param expense the expense request
     * @return the expense response
     */
    @Mapping(target = "userId", source = "user.id")
    ExpenseResponse toResponse(Expense expense);

    /**
     * Maps ExpenseRequest to Expense entity
     *
     * @param expenses the list of expense entities
     * @return the expense responses
     */
    List<ExpenseResponse> toResponseList(List<Expense> expenses);

    /**
     * Maps ExpenseRequest to Expense entity
     *
     * @param requests the list of expense requests
     * @return the expense entities
     */
    List<Expense> toEntityList(List<ExpenseRequest> requests);

    /**
     * Updates an existing Expense entity with values from ExpenseRequest
     *
     * @param request the expense request with updated values
     * @param expense the existing expense entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(ExpenseRequest request, @MappingTarget Expense expense);

    /**
     * Partial update - only updates non-null fields
     *
     * @param request the expense request with updated values
     * @param expense the existing expense entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void partialUpdateEntityFromRequest(ExpenseRequest request, @MappingTarget Expense expense);

    /**
     * Maps ExpenseRequest to Expense entity with explicit user
     *
     * @param request the expense request with updated values
     * @param user    the user to set all expenses
     * @return the expense entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "category", source = "request.category")
    @Mapping(target = "note", source = "request.note")
    @Mapping(target = "expenseType", constant = "1")
    // Default expense type
    Expense toEntityWithUser(ExpenseRequest request, AppUser user);

    /**
     * Maps list of ExpenseRequest to list of expense entities with explicit user
     *
     * @param requests the expense request with updated values
     * @param user     the user to set all expenses
     * @return the list of expense entities
     */
    default List<Expense> toEntityListWithUser(List<ExpenseRequest> requests, AppUser user) {
        return requests.stream()
                .map(request -> toEntityWithUser(request, user))
                .toList();
    }

    /**
     * Maps list of ExpenseRequest to Expense entity with userId
     *
     * @param request the expense request
     * @param userId  the user Id
     * @return the expense entity
     */
    default Expense toEntityWithUserId(ExpenseRequest request, Long userId) {
        if (userId == null) {
            return toEntity(request);
        }

        AppUser user = new AppUser();
        user.setId(userId);
        return toEntityWithUser(request, user);
    }

    /**
     * Maps list of ExpenseRequest to list of expense entities with explicit user Id
     *
     * @param requests the expense request with updated values
     * @param userId   the user Id
     * @return the list of expense entities
     */
    default List<Expense> toEntityListWithUser(List<ExpenseRequest> requests, Long userId) {
        return requests.stream()
                .map(request -> toEntityWithUserId(request, userId))
                .toList();
    }
}
