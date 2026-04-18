package com.ikrame.smartbudget.data.local.dao

import androidx.room.*
import com.ikrame.smartbudget.data.local.entity.MonthlyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): Flow<List<MonthlyBudget>>

    @Query("""
        SELECT * FROM monthly_budgets 
        WHERE month = :month AND categoryId = :categoryId
        LIMIT 1
    """)
    suspend fun getBudgetForCategory(month: String, categoryId: Long): MonthlyBudget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: MonthlyBudget)

    @Delete
    suspend fun delete(budget: MonthlyBudget)

    @Query("DELETE FROM monthly_budgets WHERE month = :month AND categoryId = :categoryId")
    suspend fun deleteForCategory(month: String, categoryId: Long)
}