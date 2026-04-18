package com.ikrame.smartbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikrame.smartbudget.data.local.AppDatabase
import com.ikrame.smartbudget.data.repository.ExpenseRepository
import com.ikrame.smartbudget.ui.AppNavigation
import com.ikrame.smartbudget.ui.screen.SplashScreen
import com.ikrame.smartbudget.ui.theme.SmartBudgetTheme
import com.ikrame.smartbudget.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val repository = ExpenseRepository(db.expenseDao(), db.categoryDao(), db.budgetDao())

        setContent {
            SmartBudgetTheme {
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModel.Factory(repository)
                )

                // Génère les dépenses récurrentes du mois courant au lancement
                LaunchedEffect(Unit) {
                    viewModel.generateRecurringExpenses()
                }

                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}