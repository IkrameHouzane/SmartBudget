package com.ikrame.smartbudget.data.local.dao

import androidx.room.*
import com.ikrame.smartbudget.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("""
        SELECT * FROM expenses 
        WHERE date >= :start AND date <= :end 
        ORDER BY date DESC
    """)
    fun getExpensesForMonth(start: String, end: String): Flow<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE date >= :start AND date <= :end 
        ORDER BY date DESC
    """)
    suspend fun getExpensesForMonthOnce(start: String, end: String): List<Expense>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE date >= :start AND date <= :end
    """)
    fun getTotalForMonth(start: String, end: String): Flow<Double>

    @Query("""
        SELECT categoryId, SUM(amount) as total FROM expenses 
        WHERE date >= :start AND date <= :end 
        GROUP BY categoryId 
        ORDER BY total DESC
    """)
    fun getTotalByCategoryForMonth(start: String, end: String): Flow<List<CategoryTotal>>

    // Toutes les dépenses récurrentes
    @Query("SELECT * FROM expenses WHERE isRecurring = 1")
    suspend fun getRecurringExpenses(): List<Expense>

    // Vérifie si une récurrente existe déjà pour ce mois
    @Query("""
        SELECT COUNT(*) FROM expenses 
        WHERE isRecurring = 1 
        AND categoryId = :categoryId 
        AND amount = :amount
        AND date >= :start AND date <= :end
    """)
    suspend fun countRecurringForMonth(
        categoryId: Long,
        amount: Double,
        start: String,
        end: String
    ): Int

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?
}

data class CategoryTotal(
    val categoryId: Long,
    val total: Double
)