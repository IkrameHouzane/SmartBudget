package com.ikrame.smartbudget.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ikrame.smartbudget.data.local.entity.Category
import com.ikrame.smartbudget.viewmodel.ExpenseViewModel

@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val allCategories  by viewModel.allCategories.collectAsState()
    val budgets        by viewModel.budgetsForMonth.collectAsState()
    val context        = LocalContext.current
    var showAddDialog  by remember { mutableStateOf(false) }
    var exportMessage  by remember { mutableStateOf<String?>(null) }
    var budgetCategory by remember { mutableStateOf<Category?>(null) }

    // Launcher pour choisir un fichier CSV
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importCSV(context, it) { imported, errors ->
                exportMessage = if (errors == 0)
                    "✅ $imported dépenses importées avec succès !"
                else
                    "⚠️ $imported importées, $errors lignes ignorées"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ── Titre ─────────────────────────────────────────
        Text(
            "Paramètres",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ── Export CSV ────────────────────────────────────
        Button(
            onClick = {
                exportMessage = null
                viewModel.exportCSV(context) { success ->
                    exportMessage = if (success)
                        "✅ Fichier exporté dans Téléchargements !"
                    else
                        "❌ Erreur lors de l'export"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Exporter le mois en CSV")
        }

        // ── Import CSV ────────────────────────────────────
        OutlinedButton(
            onClick = { importLauncher.launch("text/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Importer un fichier CSV")
        }

        // Message résultat export/import
        exportMessage?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        it.startsWith("✅") -> MaterialTheme.colorScheme.primaryContainer
                        it.startsWith("⚠️") -> MaterialTheme.colorScheme.tertiaryContainer
                        else               -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(it, modifier = Modifier.padding(12.dp))
            }
        }

        // ── Catégories + Budgets ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Catégories & Budgets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Ajouter catégorie")
            }
        }

        Text(
            "Appuie sur une catégorie pour définir son budget mensuel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allCategories) { category ->
                val budget = budgets.find { it.categoryId == category.id }
                CategoryBudgetItem(
                    category     = category,
                    budgetAmount = budget?.limitAmount,
                    onToggleActive = {
                        viewModel.updateCategory(category.copy(isActive = !category.isActive))
                    },
                    onSetBudget = { budgetCategory = category }
                )
            }
        }
    }

    // ── Dialogue ajout catégorie ───────────────────────────
    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon, color ->
                viewModel.addCategory(Category(name = name, icon = icon, color = color))
                showAddDialog = false
            }
        )
    }

    // ── Dialogue définir budget ────────────────────────────
    budgetCategory?.let { cat ->
        val currentBudget = budgets.find { it.categoryId == cat.id }
        SetBudgetDialog(
            category      = cat,
            currentAmount = currentBudget?.limitAmount,
            onDismiss     = { budgetCategory = null },
            onConfirm     = { amount ->
                viewModel.setBudget(cat.id, amount)
                budgetCategory = null
            }
        )
    }
}

// ── Item catégorie + budget ───────────────────────────────
@Composable
fun CategoryBudgetItem(
    category: Category,
    budgetAmount: Double?,
    onToggleActive: () -> Unit,
    onSetBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick  = onSetBudget
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (budgetAmount != null && budgetAmount > 0)
                        "Budget : %.0f MAD".format(budgetAmount)
                    else
                        "Aucun budget défini — appuie pour en ajouter",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (budgetAmount != null && budgetAmount > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked        = category.isActive,
                onCheckedChange = { onToggleActive() }
            )
        }
    }
}

// ── Dialogue définir budget ───────────────────────────────
@Composable
fun SetBudgetDialog(
    category: Category,
    currentAmount: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember {
        mutableStateOf(currentAmount?.let { "%.0f".format(it) } ?: "")
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget — ${category.icon} ${category.name}") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Définissez un budget mensuel pour cette catégorie.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value           = amountText,
                    onValueChange   = { amountText = it; error = null },
                    label           = { Text("Montant limite (MAD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError         = error != null,
                    supportingText  = error?.let { { Text(it) } },
                    modifier        = Modifier.fillMaxWidth(),
                    placeholder     = { Text("ex: 500") }
                )
                if (currentAmount != null && currentAmount > 0) {
                    TextButton(
                        onClick  = { onConfirm(0.0) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "🗑️ Supprimer ce budget",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    error = "Montant invalide (doit être > 0)"
                    return@Button
                }
                onConfirm(amount)
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// ── Dialogue ajout catégorie ──────────────────────────────
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name      by remember { mutableStateOf("") }
    var icon      by remember { mutableStateOf("📦") }
    var color     by remember { mutableStateOf("#FF9800") }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle catégorie") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value          = name,
                    onValueChange  = { name = it; nameError = null },
                    label          = { Text("Nom *") },
                    isError        = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier       = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = icon,
                    onValueChange = { icon = it },
                    label         = { Text("Emoji") },
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = color,
                    onValueChange = { color = it },
                    label         = { Text("Couleur hex (ex: #FF5722)") },
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    nameError = "Le nom est obligatoire"
                    return@Button
                }
                onConfirm(name.trim(), icon.trim(), color.trim())
            }) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}