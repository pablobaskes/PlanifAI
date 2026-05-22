package com.planifai.core.finance.infrastructure.output.jpa.entity;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.transaction.Recurrence;
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
@Table(name = "finance_expenses")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String concept;
    private BigDecimal amount;
    private LocalDate expenseDate;
    @Column(nullable = false, length = 40)
    private ExpenseCategory category;
    @Enumerated(EnumType.STRING)
    private Recurrence recurrence;
    private String notes;
}
