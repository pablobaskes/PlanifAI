package com.planifai.core.finance.infrastructure.output.jpa.converter;

import com.planifai.core.finance.domain.model.ExpenseCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExpenseCategoryJpaConverter implements AttributeConverter<ExpenseCategory, String> {

    @Override
    public String convertToDatabaseColumn(ExpenseCategory category) {
        return category != null ? category.name() : ExpenseCategory.OTHER.name();
    }

    @Override
    public ExpenseCategory convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return ExpenseCategory.OTHER;
        }

        return switch (value) {
            case "MORTGAGE", "RENTAL_PROPERTY" -> ExpenseCategory.HOUSING;
            case "GROCERIES" -> ExpenseCategory.FOOD;
            case "LEISURE" -> ExpenseCategory.ENTERTAINMENT;
            case "TAXES" -> ExpenseCategory.OTHER;
            default -> toCurrentCategory(value);
        };
    }

    private ExpenseCategory toCurrentCategory(String value) {
        try {
            return ExpenseCategory.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return ExpenseCategory.OTHER;
        }
    }
}
