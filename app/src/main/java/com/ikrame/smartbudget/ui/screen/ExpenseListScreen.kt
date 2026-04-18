package com.ikrame.smartbudget.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikrame.smartbudget.data.local.entity.Category
import com.ikrame.smartbudget.data.local.entity.Expense
import com.ikrame.smartbudget.viewmodel.ExpenseViewModel
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseListScreen(viewModel: ExpenseViewModel) {
    val currentDate     by viewModel.currentDate.collectAsState()
    val expenses        by viewModel.filteredExpenses.collectAsState()
    val total           by viewModel.totalForMonth.collectAsState()
    val categories      by viewModel.activeCategories.collectAsState()
    val selectedCatId   by viewModel.selectedCategoryId.collectAsState()

    var showDialog       by remember { mutableStateOf(false) }
    var expenseToEdit    by remember { mutableStateOf<Expense?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Expense?>(null) }

    val monthLabel = currentDate.month
        .getDisplayName(TextStyle.FULL, Locale.FRENCH)
        .replaceFirstChar { it.uppercase() } + " ${currentDate.year}"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                expenseToEdit = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // ── En-tête navigation mois ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                    Icon(Icons.Default.ArrowBack, "Mois précédent")
                }
                Text(
                    text  = monthLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.goToNextMonth() }) {
                    Icon(Icons.Default.ArrowForward, "Mois suivant")
                }
            }

            // ── Carte total du mois ───────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total du mois", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text  = "%.2f MAD".format(total),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Filtres par catégorie (FlowRow) ───────────
            Text(
                text = "Filtrer :",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                FilterChip(
                    selected = selectedCatId == null,
                    onClick  = { viewModel.setSelectedCategory(null) },
                    label    = { Text("Toutes") }
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick  = { viewModel.setSelectedCategory(cat.id) },
                        label    = { Text("${cat.icon} ${cat.name}") }
                    )
                }
            }

            // ── Liste des dépenses ────────────────────────
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💸", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Aucune dépense ce mois-ci",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(expenses, key = { it.id }) { expense ->
                        val category = categories.find { it.id == expense.categoryId }
                        ExpenseItem(
                            expense  = expense,
                            category = category,
                            onEdit   = { expenseToEdit = expense; showDialog = true },
                            onDelete = { showDeleteDialog = expense }
                        )
                    }
                }
            }
        }

        // ── Dialogue Ajout / Modification ─────────────────
        if (showDialog) {
            AddEditExpenseDialog(
                expense    = expenseToEdit,
                categories = categories,
                onDismiss  = { showDialog = false },
                onConfirm  = { expense ->
                    if (expenseToEdit == null) viewModel.addExpense(expense)
                    else viewModel.updateExpense(expense)
                    showDialog = false
                }
            )
        }

        // ── Dialogue confirmation suppression ─────────────
        showDeleteDialog?.let { expense ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title  = { Text("Supprimer la dépense ?") },
                text   = { Text("Cette action est irréversible.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteExpense(expense)
                        showDeleteDialog = null
                    }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Annuler") }
                }
            )
        }
    }
}

// ── Item dépense ──────────────────────────────────────────
@Composable
fun ExpenseItem(
    expense: Expense,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = parseColor(category?.color ?: "#607D8B"),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(category?.icon ?: "📦", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = category?.name ?: "Autre",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = expense.date.format(fmt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                expense.note?.let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text  = "%.2f MAD".format(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Modifier")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Supprimer",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun parseColor(hex: String): Color {
    return try { Color(android.graphics.Color.parseColor(hex)) }
    catch (e: Exception) { Color.Gray }
}