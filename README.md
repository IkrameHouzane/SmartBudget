# 💸 SmartBudget

> Application Android **offline-first** de gestion de budget personnel — Mini-Projet Module Développement Mobile 2025-2026

<p align="center">
  <img src="screenshots/splash.png" width="200" alt="Splash Screen"/>
  &nbsp;&nbsp;&nbsp;
  <img src="screenshots/depenses.png" width="200" alt="Dépenses"/>
  &nbsp;&nbsp;&nbsp;
  <img src="screenshots/stats.png" width="200" alt="Statistiques"/>
  &nbsp;&nbsp;&nbsp;
  <img src="screenshots/parametres.png" width="200" alt="Paramètres"/>
</p>

---

## 📋 Description

**SmartBudget** est une application Android native qui permet aux étudiants de **suivre leurs dépenses**, de **comprendre où part leur argent** (par catégorie et par période), et d'**exporter leurs données** pour un usage externe.

L'application fonctionne entièrement **hors connexion** (offline-first) grâce à une base de données locale Room/SQLite.

---

## ✨ Fonctionnalités

### Obligatoires ✅
| Fonctionnalité | Description |
|---|---|
| 📝 CRUD Dépenses | Ajouter, modifier, supprimer (avec confirmation) |
| 🏷️ Catégorisation | 8 catégories : Alimentation, Transport, Logement, Santé, Loisirs, Études, Vêtements, Autre |
| 📅 Filtrage temporel | Navigation mois par mois avec boutons ◀ ▶ |
| 💰 Total mensuel | Affichage du total des dépenses du mois |
| 📊 Répartition | Total et pourcentage par catégorie avec barres de progression |
| 📶 Offline-first | Fonctionnement complet sans internet |

### Bonus ⭐
| Bonus | Description |
|---|---|
| 🎯 Budgets mensuels | Définir une limite de dépense par catégorie — alerte rouge si dépassement |
| 🔄 Dépenses récurrentes | Génération automatique chaque mois (loyer, abonnements...) |
| 📤 Export CSV | Export du mois courant vers le dossier Téléchargements |
| 📥 Import CSV | Import d'un fichier CSV existant via le sélecteur natif Android |
| 📈 Comparaison N vs N-1 | Comparaison mois courant vs mois précédent avec flèche et % |
| 🎨 Splash Screen animé | Écran d'accueil avec animation de zoom au lancement |

---

## 🏗️ Architecture

L'application suit le patron **MVVM** (Model-View-ViewModel) recommandé par Google :

```
UI (Compose) ←→ ViewModel ←→ Repository ←→ DAO ←→ Room DB
```

### Structure des packages

```
com.ikrame.smartbudget/
├── data/
│   ├── local/
│   │   ├── entity/          # Category, Expense, MonthlyBudget
│   │   ├── dao/             # CategoryDao, ExpenseDao, BudgetDao
│   │   ├── AppDatabase.kt   # Room Database + pre-population
│   │   └── Converters.kt    # LocalDate TypeConverter
│   └── repository/
│       └── ExpenseRepository.kt
├── ui/
│   ├── screen/
│   │   ├── ExpenseListScreen.kt
│   │   ├── AddEditExpenseDialog.kt
│   │   ├── StatsScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── SplashScreen.kt
│   ├── theme/
│   └── AppNavigation.kt
├── viewmodel/
│   └── ExpenseViewModel.kt
└── MainActivity.kt
```

---

## 🛠️ Stack technique

| Technologie | Version | Rôle |
|---|---|---|
| **Kotlin** | 2.0.21 | Langage principal |
| **Jetpack Compose** | BOM 2024.09 | UI déclarative |
| **Material Design 3** | — | Système de design |
| **Room** | 2.8.4 | Base de données locale (SQLite) |
| **Navigation Compose** | 2.9.0 | Navigation entre écrans |
| **ViewModel** | 2.10.0 | Gestion du cycle de vie |
| **Kotlin Coroutines** | 1.10.2 | Programmation asynchrone |
| **Kotlin Flow** | — | Flux de données réactifs |
| **KSP** | 2.0.21-1.0.28 | Génération de code Room |
| **AGP** | 9.0.1 | Android Gradle Plugin |

---

## 📱 Écrans

### 1. Écran Dépenses
- En-tête avec navigation mois ◀ ▶
- Carte total du mois
- Filtres par catégorie (FlowRow)
- Liste des dépenses avec actions éditer / supprimer
- FAB pour ajouter une dépense

### 2. Formulaire Ajout / Modification
- Champs : Montant, Catégorie, Date, Note, Méthode de paiement
- Validation inline (montant > 0, format de date)
- Toggle dépense récurrente + jour du mois

### 3. Écran Statistiques
- Total dépensé du mois
- Comparaison N vs N-1 (flèche verte/rouge + %)
- Répartition par catégorie avec barres de progression
- Suivi des budgets définis (vert = OK, rouge = dépassement)

### 4. Écran Paramètres
- Export CSV du mois courant
- Import CSV existant
- Gestion des catégories (activer / désactiver)
- Définir un budget mensuel par catégorie

---

## 🚀 Installation & Lancement

### Prérequis
- Android Studio Hedgehog ou plus récent
- JDK 11+
- Android SDK API 24+

### Cloner et lancer

```bash
git clone https://github.com/ikrame/SmartBudget.git
cd SmartBudget
```

Ouvrir dans **Android Studio** → **Run** (Shift+F10)

### Données de test

L'application se pré-peuple automatiquement au **premier lancement** avec :
- 🏷️ **8 catégories** actives
- 📝 **30 dépenses** réparties sur **2 mois** (Mars et Avril 2026)

---

## 📄 Format CSV

```csv
Date,Catégorie,Montant,Devise,Note,Méthode de paiement
2026-04-01,Logement,1500.0,MAD,Loyer avril,Virement
2026-04-02,Alimentation,100.0,MAD,Courses hebdo,Espèce
```

---

## 📁 Configuration Gradle

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 36
    minSdk = 26
}
```

---

## 🧪 Tests effectués

- ✅ CRUD complet sans crash
- ✅ Validation des formulaires (montant, date)
- ✅ Navigation entre mois
- ✅ Filtrage par catégorie
- ✅ Calcul des statistiques et pourcentages
- ✅ Budgets mensuels et alertes de dépassement
- ✅ Génération automatique des dépenses récurrentes
- ✅ Export et import CSV
- ✅ Fonctionnement offline complet

---

## 👩‍💻 Auteur

**Houzane Ikrame**

---

