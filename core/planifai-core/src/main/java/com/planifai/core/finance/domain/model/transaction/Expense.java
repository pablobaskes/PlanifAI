package com.planifai.core.finance.domain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    private Long id;
    private String concept;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private Recurrence recurrence;
    private String notes;
}
