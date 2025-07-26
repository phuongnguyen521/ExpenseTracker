package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private int expenseType;
    private String date;
    private double amount;
    private String category;
    private String account;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AppUser user;

    // Using JSON / jackson
//    @JsonProperty("id")
//    private Long id;
//
//    @JsonProperty("expenseType")
//    private int expenseType;
//
//    @JsonProperty("date")
//    private String date;
//
//    @JsonProperty("amount")
//    private double amount;
//
//    @JsonProperty("category")
//    private String category;
//
//    @JsonProperty("account")
//    private String account;
//
//    @JsonProperty("note")
//    private String note;
}
