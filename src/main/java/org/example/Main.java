package org.example;

import org.example.model.Expense;
import org.example.utils.ExpenseDataLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class Main implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<Expense> expenseList = ExpenseDataLoader.getExpenses();
        expenseList.forEach(System.out::println);
    }
}