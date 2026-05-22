package com.planifai.core.finance.domain.model.budget;

import com.planifai.core.finance.domain.FinanceConstants;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    private Long id;
    private YearMonth month;
    private ExpenseCategory category;
    private BigDecimal limitAmount;
    private Boolean active;
    private String notes;

    public void validate() {
        if (month == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_MONTH_REQUIRED);
        }
        if (category == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_CATEGORY_REQUIRED);
        }
        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_LIMIT_AMOUNT_POSITIVE);
        }
    }
}
