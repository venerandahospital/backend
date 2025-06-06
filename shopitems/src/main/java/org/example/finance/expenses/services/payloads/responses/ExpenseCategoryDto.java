package org.example.finance.expenses.services.payloads.responses;

import org.example.finance.expenses.domains.ExpenseCategory;

import java.time.LocalDate;

public class ExpenseCategoryDto {

    public Long id;
    public String categoryName;
    public String description;
    public LocalDate dateOfCategoryCreation;
    public LocalDate dateOfCategoryUpdate;

    public ExpenseCategoryDto(ExpenseCategory expenseCategory) {
        this.dateOfCategoryCreation = expenseCategory.dateOfCategoryCreation;
        this.id = expenseCategory.id;
        this.categoryName = expenseCategory.categoryName;
        this.description = expenseCategory.description;
        this.dateOfCategoryUpdate = expenseCategory.dateOfCategoryUpdate;

    }
}
