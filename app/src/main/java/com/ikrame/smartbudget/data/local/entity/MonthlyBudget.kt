package com.ikrame.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class MonthlyBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val month: String,        // format "2026-04"
    val categoryId: Long,
    val limitAmount: Double
)