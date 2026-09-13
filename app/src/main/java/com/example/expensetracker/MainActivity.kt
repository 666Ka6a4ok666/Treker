package com.example.expensetracker
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.screens.DashboardScreen
import com.example.expensetracker.ui.screens.ExpensesScreen
import com.example.expensetracker.ui.screens.InventoryScreen
import com.example.expensetracker.ui.screens.SalesScreen
import com.example.expensetracker.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppScreen()
            }
        }
    }
}
@Composable
fun MainAppScreen(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") },
                    label = { Text("Дашборд") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = currentRoute == "inventory",
                    onClick = { navController.navigate("inventory") },
                    label = { Text("Склад") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = currentRoute == "sales",
                    onClick = { navController.navigate("sales") },
                    label = { Text("Продажі") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = currentRoute == "expenses",
                    onClick = { navController.navigate("expenses") },
                    label = { Text("Витрати") },
                    icon = {}
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(viewModel)
            }
            composable("inventory") {
                InventoryScreen(viewModel)
            }
            composable("sales") {
                SalesScreen(viewModel)
            }
            composable("expenses") {
                ExpensesScreen(viewModel)
            }
        }
    }
}