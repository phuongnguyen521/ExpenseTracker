package org.example.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.model.Expense;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExpenseDataLoader {
    private static List<Expense> expenses = new ArrayList<>();

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/expenses.json");
        try {
            expenses = mapper.readValue(is,
                    new TypeReference<List<Expense>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Expense> getExpenses() {
        return expenses;
    }
}
