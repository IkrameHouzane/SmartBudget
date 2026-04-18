package com.ikrame.smartbudget.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ikrame.smartbudget.data.local.entity.Category
import com.ikrame.smartbudget.data.local.entity.Expense
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    expense: Expense?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    val isEdit = expense != null
    val fmt    = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var amountText    by remember { mutableStateOf(expense?.amount?.toString() ?: "") }
    var selectedCat   by remember { mutableStateOf(
        categories.find { it.id == expense?.categoryId } ?: categories.firstOrNull()
    ) }
    var dateText      by remember { mutableStateOf(
        expense?.date?.format(fmt) ?: LocalDate.now().format(fmt)
    ) }
    var note          by remember { mutableStateOf(expense?.note ?: "") }
    var paymentMethod by remember { mutableStateOf(expense?.paymentMethod ?: "") }
    var isRecurring   by remember { mutableStateOf(expense?.isRecurring ?: false) }
    var recurringDay  by remember { mutableStateOf(
        expense?.recurringDay?.toString() ?: "1"
    ) }

    var amountError by remember { mutableStateOf<String?>(null) }
    var dateError   by remember { mutableStateOf<String?>(null) }
    var catExpanded by remember { mutableStateOf(false) }
    var pmExpanded  by remember { mutableStateOf(false) }

    val paymentMethods = listOf("Espèce", "Carte bancaire", "Virement", "Autre")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Modifier la dépense" else "Ajouter une dépense") },
        text  = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Montant ───────────────────────────────
                OutlinedTextField(
                    value           = amountText,
                    onValueChange   = { amountText = it; amountError = null },
                    label           = { Text("Montant (MAD) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError         = amountError != null,
                    supportingText  = amountError?.let { { Text(it) } },
                    modifier        = Modifier.fillMaxWidth()
                )

                // ── Catégorie ─────────────────────────────
                ExposedDropdownMenuBox(
                    expanded         = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = selectedCat?.let { "${it.icon} ${it.name}" } ?: "Choisir",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Catégorie *") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded         = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text("${cat.icon} ${cat.name}") },
                                onClick = { selectedCat = cat; catExpanded = false }
                            )
                        }
                    }
                }

                // ── Date ──────────────────────────────────
                OutlinedTextField(
                    value          = dateText,
                    onValueChange  = { dateText = it; dateError = null },
                    label          = { Text("Date (jj/MM/aaaa) *") },
                    isError        = dateError != null,
                    supportingText = dateError?.let { { Text(it) } },
                    modifier       = Modifier.fillMaxWidth()
                )

                // ── Note ──────────────────────────────────
                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Note (optionnel)") },
                    modifier      = Modifier.fillMaxWidth()
                )

                // ── Méthode de paiement ───────────────────
                ExposedDropdownMenuBox(
                    expanded         = pmExpanded,
                    onExpandedChange = { pmExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = paymentMethod.ifEmpty { "Non précisé" },
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Méthode de paiement") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(pmExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded         = pmExpanded,
                        onDismissRequest = { pmExpanded = false }
                    ) {
                        paymentMethods.forEach { pm ->
                            DropdownMenuItem(
                                text    = { Text(pm) },
                                onClick = { paymentMethod = pm; pmExpanded = false }
                            )
                        }
                    }
                }

                // ── Dépense récurrente ────────────────────
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "🔄 Dépense récurrente",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Se répète chaque mois automatiquement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked  = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }

                // Jour du mois (visible seulement si récurrente)
                if (isRecurring) {
                    OutlinedTextField(
                        value          = recurringDay,
                        onValueChange  = { recurringDay = it },
                        label          = { Text("Jour du mois (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier       = Modifier.fillMaxWidth(),
                        supportingText = { Text("Ex: 1 pour le 1er de chaque mois") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    amountError = "Montant invalide (doit être > 0)"
                    return@Button
                }
                val date = try {
                    LocalDate.parse(dateText, fmt)
                } catch (e: Exception) {
                    dateError = "Format invalide (jj/MM/aaaa)"
                    return@Button
                }
                if (selectedCat == null) return@Button

                val day = if (isRecurring)
                    recurringDay.toIntOrNull()?.coerceIn(1, 31) ?: 1
                else null

                val result = expense?.copy(
                    amount        = amount,
                    categoryId    = selectedCat!!.id,
                    date          = date,
                    note          = note.ifEmpty { null },
                    paymentMethod = paymentMethod.ifEmpty { null },
                    isRecurring   = isRecurring,
                    recurringDay  = day,
                    updatedAt     = System.currentTimeMillis()
                ) ?: Expense(
                    amount        = amount,
                    categoryId    = selectedCat!!.id,
                    date          = date,
                    note          = note.ifEmpty { null },
                    paymentMethod = paymentMethod.ifEmpty { null },
                    isRecurring   = isRecurring,
                    recurringDay  = day
                )
                onConfirm(result)
            }) { Text(if (isEdit) "Modifier" else "Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}