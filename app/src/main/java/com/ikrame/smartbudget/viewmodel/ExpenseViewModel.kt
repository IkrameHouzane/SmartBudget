@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.ikrame.smartbudget.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ikrame.smartbudget.data.local.dao.CategoryTotal
import com.ikrame.smartbudget.data.local.entity.Category
import com.ikrame.smartbudget.data.local.entity.Expense
import com.ikrame.smartbudget.data.local.entity.MonthlyBudget
import com.ikrame.smartbudget.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // ── Mois courant ───────────────────────────────────────
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    // ── Filtre catégorie ───────────────────────────────────
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    // ── Dépenses du mois ───────────────────────────────────
    val expenses: StateFlow<List<Expense>> = _currentDate.flatMapLatest { date ->
        repository.getExpensesForMonth(date.year, date.monthValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Total du mois ──────────────────────────────────────
    val totalForMonth: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        repository.getTotalForMonth(date.year, date.monthValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ── Total mois précédent ───────────────────────────────
    val totalPreviousMonth: StateFlow<Double> = _currentDate.flatMapLatest { date ->
        val prev = date.minusMonths(1)
        repository.getTotalForMonthSimple(prev.year, prev.monthValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ── Total par catégorie ────────────────────────────────
    val totalByCategory: StateFlow<List<CategoryTotal>> = _currentDate.flatMapLatest { date ->
        repository.getTotalByCategoryForMonth(date.year, date.monthValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Catégories ─────────────────────────────────────────
    val activeCategories: StateFlow<List<Category>> =
        repository.getActiveCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> =
        repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Dépenses filtrées ──────────────────────────────────
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        expenses, _selectedCategoryId
    ) { list, catId ->
        if (catId == null) list else list.filter { it.categoryId == catId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Budgets du mois ────────────────────────────────────
    val budgetsForMonth: StateFlow<List<MonthlyBudget>> = _currentDate.flatMapLatest { date ->
        repository.getBudgetsForMonth(date.year, date.monthValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Navigation mois ────────────────────────────────────
    fun goToPreviousMonth() {
        _currentDate.value = _currentDate.value.minusMonths(1)
        generateRecurringExpenses()
    }

    fun goToNextMonth() {
        _currentDate.value = _currentDate.value.plusMonths(1)
        generateRecurringExpenses()
    }

    fun setSelectedCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    // ── CRUD Dépenses ──────────────────────────────────────
    fun addExpense(expense: Expense) =
        viewModelScope.launch { repository.addExpense(expense) }

    fun updateExpense(expense: Expense) =
        viewModelScope.launch { repository.updateExpense(expense) }

    fun deleteExpense(expense: Expense) =
        viewModelScope.launch { repository.deleteExpense(expense) }

    suspend fun getExpenseById(id: Long): Expense? = repository.getExpenseById(id)

    // ── CRUD Catégories ────────────────────────────────────
    fun addCategory(category: Category) =
        viewModelScope.launch { repository.addCategory(category) }

    fun updateCategory(category: Category) =
        viewModelScope.launch { repository.updateCategory(category) }

    fun deleteCategory(category: Category, onResult: (Boolean) -> Unit) =
        viewModelScope.launch {
            val success = repository.deleteCategory(category)
            onResult(success)
        }

    // ── Budgets ────────────────────────────────────────────
    fun setBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val date = _currentDate.value
            repository.setBudget(date.year, date.monthValue, categoryId, amount)
        }
    }

    // ── Dépenses récurrentes ───────────────────────────────
    fun generateRecurringExpenses() {
        viewModelScope.launch {
            val date = _currentDate.value
            repository.generateRecurringExpenses(date.year, date.monthValue)
        }
    }

    // ── Export CSV ─────────────────────────────────────────
    fun exportCSV(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val date       = _currentDate.value
            val categories = activeCategories.value
            val success    = repository.exportMonthToCSV(
                context, date.year, date.monthValue, categories
            )
            onResult(success)
        }
    }

    // ── Import CSV ─────────────────────────────────────────
    fun importCSV(context:
                  Context, uri: Uri, onResult: (Int, Int) -> Unit) {
        viewModelScope.launch {
            val (imported, errors) = repository.importFromCSV(context, uri)
            onResult(imported, errors)
        }
    }

    // ── Factory ────────────────────────────────────────────
    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
    }
}