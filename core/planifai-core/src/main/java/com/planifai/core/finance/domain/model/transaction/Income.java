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
public class Income {

    private Long id;
    private String source;
    private BigDecimal amount;
    private LocalDate incomeDate;
    private IncomeCategory category;
    private Recurrence recurrence;
    private String notes;
}
