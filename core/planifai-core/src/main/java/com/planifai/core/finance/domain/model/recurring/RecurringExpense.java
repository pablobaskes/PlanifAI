package com.planifai.core.finance.domain.model.recurring;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
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
public class RecurringExpense {

    private Long id;
    private String name;
    private BigDecimal amount;
    private ExpenseCategory category;
    private RecurringExpenseRecurrence recurrence;
    private Integer paymentDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private String notes;
}
