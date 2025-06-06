package org.example.finance.expenses.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.expenses.domains.ExpenseCategory;
import org.example.finance.expenses.domains.repositories.ExpenseCategoryRepository;
import org.example.finance.expenses.services.payloads.requests.ExpenseCategoryRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseCategoryDto;

import java.time.LocalDate;

@ApplicationScoped
public class ExpenseCategoryService {


    @Inject
    ExpenseCategoryRepository expenseCategoryRepository;

    public Response createExpenseCategory(ExpenseCategoryRequest request){

        ExpenseCategory expenseCategory = new ExpenseCategory();
        expenseCategory.categoryName = request.categoryName;
        expenseCategory.description = request.description;
        expenseCategory.dateOfCategoryCreation = LocalDate.now();
        expenseCategory.dateOfCategoryUpdate = LocalDate.now();

        expenseCategoryRepository.persist(expenseCategory);

        return Response.ok(new ResponseMessage("New Expense Category created successfully", new ExpenseCategoryDto(expenseCategory))).build();

    }
}
