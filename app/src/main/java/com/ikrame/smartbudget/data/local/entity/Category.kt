package com.ikrame.smartbudget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,               // "Alimentation", "Transport"...
    val icon: String = "📦",        // emoji
    val color: String = "#FF9800",  // couleur hex pour l'UI
    val isActive: Boolean = true    // permet d'archiver sans supprimer
)