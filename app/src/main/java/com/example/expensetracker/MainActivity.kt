package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.products.ProductViewModel
import com.example.expensetracker.ui.products.ProductsScreen
import com.example.expensetracker.ui.repairs.RepairsScreen
import com.example.expensetracker.ui.repairs.RepairsViewModel
import com.example.expensetracker.ui.sales.SalesViewModel
import com.example.expensetracker.ui.screens.DashboardScreen
import com.example.expensetracker.ui.screens.ExpensesScreen
import com.example.expensetracker.ui.screens.IncomeScreen
import com.example.expensetracker.ui.screens.SalesScreen
import com.example.expensetracker.ui.theme.TrackerTheme
import com.example.expensetracker.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen()
                }
            }
        }
    }
}

sealed class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : NavItem("dashboard", "Дашборд", Icons.Filled.Home, Icons.Outlined.Home)
    data object Inventory : NavItem("inventory", "Склад", Icons.Filled.Inventory2, Icons.Outlined.Inventory2)
    data object Sales : NavItem("sales", "Продажі", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale)
    data object Expenses : NavItem("expenses", "Витрати", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong)
    data object Income : NavItem("income", "Кошти", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism)
    data object Repairs : NavItem("repairs", "Ремонти", Icons.Filled.Build, Icons.Outlined.Build)
    data object Production : NavItem("production", "Виробництво", Icons.Filled.Factory, Icons.Outlined.Factory)
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavItem.Dashboard.route

    val navItems = listOf(
        NavItem.Dashboard,
        NavItem.Inventory,
        NavItem.Sales,
        NavItem.Expenses,
        NavItem.Income,
        NavItem.Repairs
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,

                tonalElevation = NavigationBarDefaults.Elevation,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.Dashboard.route) {
                val viewModel: MainViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSales = { navController.navigate(NavItem.Sales.route) },
                    onNavigateToExpenses = { navController.navigate(NavItem.Expenses.route) },
                    onNavigateToInventory = { navController.navigate(NavItem.Inventory.route) },
                    onNavigateToIncome = { navController.navigate(NavItem.Income.route) },
                    onNavigateToRepairs = { navController.navigate(NavItem.Repairs.route) }
                )
            }
            composable(NavItem.Repairs.route) {
                val viewModel: RepairsViewModel = hiltViewModel()
                RepairsScreen(viewModel)
            }
            composable(NavItem.Inventory.route) {
                val viewModel: ProductViewModel = hiltViewModel()
                ProductsScreen(viewModel)
            }
            composable(NavItem.Sales.route) {
                val viewModel: SalesViewModel = hiltViewModel()
                SalesScreen(viewModel)
            }
            composable(NavItem.Expenses.route) {
                val viewModel: MainViewModel = hiltViewModel()
                ExpensesScreen(viewModel)
            }
            composable(NavItem.Income.route) {
                val viewModel: MainViewModel = hiltViewModel()
                IncomeScreen(viewModel)
            }
        }
    }
}
