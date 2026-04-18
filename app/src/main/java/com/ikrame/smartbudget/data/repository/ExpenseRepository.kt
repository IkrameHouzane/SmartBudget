package com.ikrame.smartbudget.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.ikrame.smartbudget.data.local.dao.BudgetDao
import com.ikrame.smartbudget.data.local.dao.CategoryDao
import com.ikrame.smartbudget.data.local.dao.CategoryTotal
import com.ikrame.smartbudget.data.local.dao.ExpenseDao
import com.ikrame.smartbudget.data.local.entity.Category
import com.ikrame.smartbudget.data.local.entity.Expense
import com.ikrame.smartbudget.data.local.entity.MonthlyBudget
import kotlinx.coroutines.flow.Flow
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    // ── Helpers dates ──────────────────────────────────────
    private fun startOf(year: Int, month: Int): String =
        LocalDate.of(year, month, 1).format(fmt)

    private fun endOf(year: Int, month: Int): String =
        LocalDate.of(year, month, 1)
            .withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth())
            .format(fmt)

    private fun monthStr(year: Int, month: Int): String =
        "%04d-%02d".format(year, month)

    // ── Dépenses ───────────────────────────────────────────
    fun getExpensesForMonth(year: Int, month: Int): Flow<List<Expense>> =
        expenseDao.getExpensesForMonth(startOf(year, month), endOf(year, month))

    fun getTotalForMonth(year: Int, month: Int): Flow<Double> =
        expenseDao.getTotalForMonth(startOf(year, month), endOf(year, month))

    fun getTotalForMonthSimple(year: Int, month: Int): Flow<Double> =
        expenseDao.getTotalForMonth(startOf(year, month), endOf(year, month))

    fun getTotalByCategoryForMonth(year: Int, month: Int): Flow<List<CategoryTotal>> =
        expenseDao.getTotalByCategoryForMonth(startOf(year, month), endOf(year, month))

    suspend fun addExpense(expense: Expense)    = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
    suspend fun getExpenseById(id: Long)        = expenseDao.getById(id)

    // ── Catégories ─────────────────────────────────────────
    fun getActiveCategories(): Flow<List<Category>> = categoryDao.getAllActive()
    fun getAllCategories(): Flow<List<Category>>     = categoryDao.getAll()

    suspend fun addCategory(category: Category)    = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category): Boolean {
        val count = categoryDao.countExpensesForCategory(category.id)
        return if (count == 0) { categoryDao.delete(category); true } else false
    }

    // ── Budgets ────────────────────────────────────────────
    fun getBudgetsForMonth(year: Int, month: Int): Flow<List<MonthlyBudget>> =
        budgetDao.getBudgetsForMonth(monthStr(year, month))

    suspend fun setBudget(year: Int, month: Int, categoryId: Long, amount: Double) {
        val ms = monthStr(year, month)
        if (amount <= 0) {
            budgetDao.deleteForCategory(ms, categoryId)
        } else {
            budgetDao.insertOrUpdate(
                MonthlyBudget(
                    month       = ms,
                    categoryId  = categoryId,
                    limitAmount = amount
                )
            )
        }
    }

    // ── Dépenses récurrentes ───────────────────────────────
    suspend fun generateRecurringExpenses(year: Int, month: Int) {
        val recurring = expenseDao.getRecurringExpenses()
        val start     = startOf(year, month)
        val end       = endOf(year, month)

        recurring.forEach { expense ->
            val day   = expense.recurringDay ?: 1
            val count = expenseDao.countRecurringForMonth(
                expense.categoryId, expense.amount, start, end
            )
            if (count == 0) {
                val daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()
                val actualDay   = minOf(day, daysInMonth)
                expenseDao.insert(
                    expense.copy(
                        id        = 0,
                        date      = LocalDate.of(year, month, actualDay),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // ── Export CSV ─────────────────────────────────────────
    suspend fun exportMonthToCSV(
        context: Context,
        year: Int,
        month: Int,
        categories: List<Category>
    ): Boolean {
        return try {
            val expenses = expenseDao.getExpensesForMonthOnce(startOf(year, month), endOf(year, month))
            val catMap   = categories.associateBy { it.id }
            val fileName = "SmartBudget_${year}_${"%02d".format(month)}.csv"

            val csvContent = buildString {
                appendLine("Date,Catégorie,Montant,Devise,Note,Méthode de paiement")
                expenses.forEach { exp ->
                    val catName = catMap[exp.categoryId]?.name ?: "Autre"
                    val note    = exp.note?.replace(",", ";") ?: ""
                    val pm      = exp.paymentMethod?.replace(",", ";") ?: ""
                    appendLine("${exp.date},${catName},${exp.amount},${exp.currency},${note},${pm}")
                }
            }

            val outputStream: OutputStream? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver      = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = resolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
                    )
                    uri?.let { resolver.openOutputStream(it) }
                } else {
                    val file = java.io.File(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ),
                        fileName
                    )
                    java.io.FileOutputStream(file)
                }

            outputStream?.use { it.write(csvContent.toByteArray()) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ── Import CSV ─────────────────────────────────────────
    suspend fun importFromCSV(context: Context, uri: Uri): Pair<Int, Int> {
        var imported = 0
        var errors   = 0

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Pair(0, 0)
            val lines = inputStream.bufferedReader().readLines()
            inputStream.close()

            val categories = categoryDao.getAllOnce()
            val catMap     = categories.associateBy { it.name.lowercase() }

            // Ignore la première ligne (en-tête)
            lines.drop(1).forEach { line ->
                try {
                    if (line.isBlank()) return@forEach
                    val cols = line.split(",")
                    if (cols.size < 3) { errors++; return@forEach }

                    val date    = LocalDate.parse(
                        cols[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    val catName = cols[1].trim().lowercase()
                    val amount  = cols[2].trim().toDoubleOrNull()
                        ?: run { errors++; return@forEach }
                    val currency = if (cols.size > 3)
                        cols[3].trim().ifEmpty { "MAD" } else "MAD"
                    val note     = if (cols.size > 4)
                        cols[4].trim().replace(";", ",").ifEmpty { null } else null
                    val pm       = if (cols.size > 5)
                        cols[5].trim().replace(";", ",").ifEmpty { null } else null

                    val category = catMap[catName]
                        ?: catMap["autre"]
                        ?: categories.firstOrNull()
                        ?: run { errors++; return@forEach }

                    expenseDao.insert(
                        Expense(
                            amount        = amount,
                            currency      = currency,
                            date          = date,
                            categoryId    = category.id,
                            note          = note,
                            paymentMethod = pm
                        )
                    )
                    imported++
                } catch (e: Exception) {
                    errors++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(imported, errors)
    }
}