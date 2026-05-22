package com.planifai.core.finance.infrastructure.output.jpa.entity;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "finance_budgets",
        indexes = {
                @Index(name = "idx_finance_budgets_month", columnList = "budget_month"),
                @Index(name = "idx_finance_budgets_month_category_active", columnList = "budget_month, category, active")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "budget_month", nullable = false, length = 7)
    private String month;
    @Column(nullable = false, length = 40)
    private ExpenseCategory category;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limitAmount;
    @Column(nullable = false)
    private Boolean active;
    @Column(length = 1000)
    private String notes;
}
