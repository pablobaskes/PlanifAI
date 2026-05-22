package com.planifai.core.finance.infrastructure.output.jpa.converter;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseCategoryJpaConverterTest {

    private final ExpenseCategoryJpaConverter converter = new ExpenseCategoryJpaConverter();

    @Test
    void persistsCurrentCategoryValue() {
        assertEquals("SUBSCRIPTIONS", converter.convertToDatabaseColumn(ExpenseCategory.SUBSCRIPTIONS));
    }

    @Test
    void mapsLegacyStoredValuesToCurrentCategories() {
        assertEquals(ExpenseCategory.HOUSING, converter.convertToEntityAttribute("MORTGAGE"));
        assertEquals(ExpenseCategory.HOUSING, converter.convertToEntityAttribute("RENTAL_PROPERTY"));
        assertEquals(ExpenseCategory.FOOD, converter.convertToEntityAttribute("GROCERIES"));
        assertEquals(ExpenseCategory.ENTERTAINMENT, converter.convertToEntityAttribute("LEISURE"));
    }

    @Test
    void mapsUnknownStoredValuesToOther() {
        assertEquals(ExpenseCategory.OTHER, converter.convertToEntityAttribute("UNKNOWN"));
        assertEquals(ExpenseCategory.OTHER, converter.convertToEntityAttribute(null));
    }
}
