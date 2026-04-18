package com.ikrame.smartbudget.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikrame.smartbudget.viewmodel.ExpenseViewModel
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(viewModel: ExpenseViewModel) {
    val currentDate    by viewModel.currentDate.collectAsState()
    val total          by viewModel.totalForMonth.collectAsState()
    val totalPrev      by viewModel.totalPreviousMonth.collectAsState()
    val categoryTotals by viewModel.totalByCategory.collectAsState()
    val categories     by viewModel.activeCategories.collectAsState()
    val budgets        by viewModel.budgetsForMonth.collectAsState()

    val monthLabel = currentDate.month
        .getDisplayName(TextStyle.FULL, Locale.FRENCH)
        .replaceFirstChar { it.uppercase() } + " ${currentDate.year}"

    val prevMonthLabel = currentDate.minusMonths(1).month
        .getDisplayName(TextStyle.FULL, Locale.FRENCH)
        .replaceFirstChar { it.uppercase() }

    val diff       = total - totalPrev
    val diffPct    = if (totalPrev > 0) (diff / totalPrev * 100) else 0.0
    val isIncrease = diff > 0
    val isEqual    = diff == 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Titre ──────────────────────────────────────────
        item {
            Text(
                text  = "Statistiques — $monthLabel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Total du mois ──────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total dépensé", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text  = "%.2f MAD".format(total),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Comparaison mois N vs N-1 ──────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = when {
                        isEqual    -> MaterialTheme.colorScheme.surfaceVariant
                        isIncrease -> Color(0xFFFFEBEE)
                        else       -> Color(0xFFE8F5E9)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Comparaison avec $prevMonthLabel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Mois précédent
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                prevMonthLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "%.2f MAD".format(totalPrev),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Flèche + différence
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when {
                                    isEqual    -> Icons.Default.Remove
                                    isIncrease -> Icons.Default.ArrowUpward
                                    else       -> Icons.Default.ArrowDownward
                                },
                                contentDescription = null,
                                tint = when {
                                    isEqual    -> Color.Gray
                                    isIncrease -> Color(0xFFE53935)
                                    else       -> Color(0xFF43A047)
                                },
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isEqual) "="
                                else "${"%.1f".format(kotlin.math.abs(diffPct))}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isEqual    -> Color.Gray
                                    isIncrease -> Color(0xFFE53935)
                                    else       -> Color(0xFF43A047)
                                }
                            )
                        }

                        // Mois actuel
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                monthLabel.split(" ")[0],
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "%.2f MAD".format(total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = when {
                            isEqual    -> "Même budget que le mois précédent"
                            isIncrease -> "⚠️ +%.2f MAD de plus que $prevMonthLabel".format(diff)
                            else       -> "✅ %.2f MAD d'économies vs $prevMonthLabel".format(-diff)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Répartition par catégorie ──────────────────────
        item {
            Text(
                text  = "Répartition par catégorie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (categoryTotals.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aucune donnée pour ce mois",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(categoryTotals) { catTotal ->
                val category     = categories.find { it.id == catTotal.categoryId }
                val percentage   = if (total > 0) (catTotal.total / total * 100) else 0.0
                val budget       = budgets.find { it.categoryId == catTotal.categoryId }
                val isOverBudget = budget != null && catTotal.total > budget.limitAmount
                val budgetPct    = if (budget != null && budget.limitAmount > 0)
                    (catTotal.total / budget.limitAmount * 100) else 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = if (isOverBudget)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = parseColor(category?.color ?: "#607D8B"),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(category?.icon ?: "📦")
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text  = category?.name ?: "Autre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text  = "%.2f MAD".format(catTotal.total),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                if (budget != null) {
                                    Text(
                                        text  = "/ %.0f MAD".format(budget.limitAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text  = "%.1f%%".format(percentage),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isOverBudget) {
                                Spacer(Modifier.width(4.dp))
                                Text("⚠️")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Barre budget ou barre % du total
                        if (budget != null) {
                            LinearProgressIndicator(
                                progress = {
                                    budgetPct.toFloat().div(100).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (isOverBudget)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isOverBudget)
                                    "⚠️ Dépassement de %.2f MAD".format(
                                        catTotal.total - budget.limitAmount
                                    )
                                else
                                    "✅ %.2f MAD restants".format(
                                        budget.limitAmount - catTotal.total
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverBudget)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = {
                                    (percentage / 100).toFloat().coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = parseColor(category?.color ?: "#607D8B")
                            )
                        }
                    }
                }
            }
        }
    }
}