package com.planifai.core.finance.infrastructure.output.jpa.entity;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.recurring.RecurringExpenseRecurrence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "finance_recurring_expenses")
@Getter
@Setter
@NoArgsConstructor
public class RecurringExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 40)
    private ExpenseCategory category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RecurringExpenseRecurrence recurrence;
    @Column(nullable = false)
    private Integer paymentDay;
    @Column(nullable = false)
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false)
    private Boolean active;
    @Column(length = 1000)
    private String notes;
}
