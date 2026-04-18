package com.ikrame.smartbudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ikrame.smartbudget.data.local.dao.BudgetDao
import com.ikrame.smartbudget.data.local.dao.CategoryDao
import com.ikrame.smartbudget.data.local.dao.ExpenseDao
import com.ikrame.smartbudget.data.local.entity.Category
import com.ikrame.smartbudget.data.local.entity.Expense
import com.ikrame.smartbudget.data.local.entity.MonthlyBudget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [Category::class, Expense::class, MonthlyBudget::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbudget_db"
                )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                // Vérifie si la DB est vide via le DAO
                                val count = database.categoryDao().countAll()
                                if (count == 0) {
                                    prepopulate(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulate(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val expenseDao  = db.expenseDao()

            val categories = listOf(
                Category(name = "Alimentation", icon = "🍔", color = "#FF5722"),
                Category(name = "Transport",    icon = "🚌", color = "#2196F3"),
                Category(name = "Logement",     icon = "🏠", color = "#9C27B0"),
                Category(name = "Santé",        icon = "💊", color = "#F44336"),
                Category(name = "Loisirs",      icon = "🎮", color = "#4CAF50"),
                Category(name = "Études",       icon = "📚", color = "#FF9800"),
                Category(name = "Vêtements",    icon = "👗", color = "#E91E63"),
                Category(name = "Autre",        icon = "📦", color = "#607D8B")
            )
            val ids = categories.map { categoryDao.insert(it) }

            val expenses = listOf(
                // Mars 2026
                Expense(amount = 120.0,  date = LocalDate.of(2026,3,1),  categoryId = ids[0], note = "Courses Marjane"),
                Expense(amount = 45.0,   date = LocalDate.of(2026,3,2),  categoryId = ids[1], note = "Bus semaine"),
                Expense(amount = 1500.0, date = LocalDate.of(2026,3,3),  categoryId = ids[2], note = "Loyer mars"),
                Expense(amount = 80.0,   date = LocalDate.of(2026,3,5),  categoryId = ids[0], note = "Restaurant"),
                Expense(amount = 200.0,  date = LocalDate.of(2026,3,7),  categoryId = ids[5], note = "Livres universite"),
                Expense(amount = 60.0,   date = LocalDate.of(2026,3,8),  categoryId = ids[4], note = "Netflix + Spotify"),
                Expense(amount = 35.0,   date = LocalDate.of(2026,3,10), categoryId = ids[1], note = "Taxi"),
                Expense(amount = 150.0,  date = LocalDate.of(2026,3,12), categoryId = ids[3], note = "Pharmacie"),
                Expense(amount = 90.0,   date = LocalDate.of(2026,3,14), categoryId = ids[0], note = "Epicerie"),
                Expense(amount = 250.0,  date = LocalDate.of(2026,3,16), categoryId = ids[6], note = "Chaussures"),
                Expense(amount = 40.0,   date = LocalDate.of(2026,3,18), categoryId = ids[1], note = "Grand taxi"),
                Expense(amount = 75.0,   date = LocalDate.of(2026,3,20), categoryId = ids[4], note = "Cinema + sortie"),
                Expense(amount = 110.0,  date = LocalDate.of(2026,3,22), categoryId = ids[0], note = "Supermarche"),
                Expense(amount = 30.0,   date = LocalDate.of(2026,3,25), categoryId = ids[7], note = "Divers"),
                Expense(amount = 55.0,   date = LocalDate.of(2026,3,28), categoryId = ids[5], note = "Impression cours"),
                // Avril 2026
                Expense(amount = 1500.0, date = LocalDate.of(2026,4,1),  categoryId = ids[2], note = "Loyer avril"),
                Expense(amount = 100.0,  date = LocalDate.of(2026,4,2),  categoryId = ids[0], note = "Courses hebdo"),
                Expense(amount = 50.0,   date = LocalDate.of(2026,4,3),  categoryId = ids[1], note = "Transport semaine"),
                Expense(amount = 200.0,  date = LocalDate.of(2026,4,5),  categoryId = ids[3], note = "Dentiste"),
                Expense(amount = 85.0,   date = LocalDate.of(2026,4,6),  categoryId = ids[0], note = "Cafe + resto"),
                Expense(amount = 180.0,  date = LocalDate.of(2026,4,8),  categoryId = ids[6], note = "Veste"),
                Expense(amount = 60.0,   date = LocalDate.of(2026,4,9),  categoryId = ids[4], note = "Jeu mobile"),
                Expense(amount = 45.0,   date = LocalDate.of(2026,4,11), categoryId = ids[1], note = "Bus + taxi"),
                Expense(amount = 130.0,  date = LocalDate.of(2026,4,12), categoryId = ids[5], note = "Cours en ligne"),
                Expense(amount = 95.0,   date = LocalDate.of(2026,4,13), categoryId = ids[0], note = "Supermarche"),
                Expense(amount = 70.0,   date = LocalDate.of(2026,4,14), categoryId = ids[4], note = "Sortie weekend"),
                Expense(amount = 25.0,   date = LocalDate.of(2026,4,15), categoryId = ids[7], note = "Divers"),
                Expense(amount = 300.0,  date = LocalDate.of(2026,4,16), categoryId = ids[3], note = "Analyse medicale"),
                Expense(amount = 40.0,   date = LocalDate.of(2026,4,16), categoryId = ids[1], note = "Uber"),
                Expense(amount = 115.0,  date = LocalDate.of(2026,4,16), categoryId = ids[0], note = "Courses fin de semaine")
            )
            expenses.forEach { expenseDao.insert(it) }
        }
    }
}