package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.IncomeCategory;
import com.planifai.core.finance.domain.model.Recurrence;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinanceUseCase implements FinanceInputPort {

    private final ExpenseOutputPort expenseOutputPort;
    private final IncomeOutputPort incomeOutputPort;

    public FinanceUseCase(ExpenseOutputPort expenseOutputPort, IncomeOutputPort incomeOutputPort) {
        this.expenseOutputPort = expenseOutputPort;
        this.incomeOutputPort = incomeOutputPort;
    }

    @Override
    public List<Expense> getExpenses() {
        return expenseOutputPort.findAll();
    }

    @Override
    public Expense createExpense(Expense expense) {
        validateExpense(expense);
        expense.setId(null);
        if (expense.getCategory() == null) {
            expense.setCategory(ExpenseCategory.OTHER);
        }
        if (expense.getRecurrence() == null) {
            expense.setRecurrence(Recurrence.ONE_OFF);
        }
        return expenseOutputPort.save(expense);
    }

    @Override
    public List<Income> getIncomes() {
        return incomeOutputPort.findAll();
    }

    @Override
    public Income createIncome(Income income) {
        validateIncome(income);
        income.setId(null);
        if (income.getCategory() == null) {
            income.setCategory(IncomeCategory.OTHER);
        }
        if (income.getRecurrence() == null) {
            income.setRecurrence(Recurrence.ONE_OFF);
        }
        return incomeOutputPort.save(income);
    }

    private void validateExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense is required.");
        }
        if (expense.getConcept() == null || expense.getConcept().isBlank()) {
            throw new IllegalArgumentException("Expense concept is required.");
        }
        validateAmount(expense.getAmount(), "Expense amount must be positive.");
        if (expense.getExpenseDate() == null) {
            throw new IllegalArgumentException("Expense date is required.");
        }
    }

    private void validateIncome(Income income) {
        if (income == null) {
            throw new IllegalArgumentException("Income is required.");
        }
        if (income.getSource() == null || income.getSource().isBlank()) {
            throw new IllegalArgumentException("Income source is required.");
        }
        validateAmount(income.getAmount(), "Income amount must be positive.");
        if (income.getIncomeDate() == null) {
            throw new IllegalArgumentException("Income date is required.");
        }
    }

    private void validateAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
