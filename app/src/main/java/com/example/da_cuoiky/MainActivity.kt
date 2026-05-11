package com.example.da_cuoiky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.da_cuoiky.fiebase.AuthViewModel
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.navigation.Screen
import com.example.da_cuoiky.ui.screens.*
import com.example.da_cuoiky.ui.theme.DA_CuoiKyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DA_CuoiKyTheme {
                RestaurantApp()
            }
        }
    }
}

@Composable
fun RestaurantApp() {
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()
    var cartItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var lastOrderId by remember { mutableStateOf<String?>(null) }
    var deliveryType by remember { mutableStateOf(DeliveryType.PICKUP) }
    var deliveryAddress by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = Screen.CustomerMain.route
    ) {
        // ── AUTH ──
        composable(Screen.Login.route) {
            LoginScreen(
                navController        = navController,
                viewModel            = authViewModel,
                onRoleSelected       = { },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // ── STAFF ──
        composable(Screen.StaffFloorPlan.route) {
            FloorPlanScreen(
                onTableClick = { table ->
                    if (table.status == TableStatus.OCCUPIED) {
                        val targetId = if (table.currentOrderId.isNotEmpty()) table.currentOrderId else "POS_${table.id}"
                        navController.navigate(Screen.StaffPOS.buildRoute(targetId))
                    }
                },
                onNavigateToOrder = { tableId -> navController.navigate(Screen.StaffOrder.buildRoute(tableId)) }
            )
        }

        composable(Screen.StaffOrder.route) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getString("tableId") ?: "T01"
            OrderScreen(
                tableId = tableId,
                tableName = "Bàn",
                onSendToKitchen = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StaffKitchen.route) {
            KitchenScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.StaffPOS.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: "O1001"
            POSScreen(
                order = SampleData.sampleOrder.copy(id = orderId),
                onPaymentComplete = { _ ->
                    navController.navigate(Screen.StaffFloorPlan.route) { popUpTo(Screen.StaffFloorPlan.route) { inclusive = false } }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── CUSTOMER ──
        composable(Screen.CustomerMain.route) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab")
            CustomerMainScreen(
                navController  = navController,
                authViewModel  = authViewModel,
                initialTab     = CustomerTab.HOME,
                cartItems      = cartItems,
                onAddToCart    = { item -> cartItems = cartItems + item },
                lastOrderId    = lastOrderId,
                targetTab      = tab
            )
        }

        composable(Screen.ProductDetail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: "M01"
            ProductDetailScreen(
                itemId = itemId,
                onAddToCart = { item -> cartItems = cartItems + item },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerCart.route) {
            CartScreen(
                cartItems = cartItems,
                authViewModel = authViewModel,
                deliveryType = deliveryType,
                onDeliveryTypeChange = { deliveryType = it },
                deliveryAddress = deliveryAddress,
                onAddressChange = { deliveryAddress = it },
                onQtyChange = { item, newQty ->
                    cartItems = if (newQty > 0) {
                        cartItems.map { if (it.menuItemId == item.menuItemId) it.copy(qty = newQty) else it }
                    } else {
                        cartItems.filter { it.menuItemId != item.menuItemId }
                    }
                },
                onCheckout = { navController.navigate(Screen.CustomerCheckout.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerCheckout.route) {
            CustomerCheckoutScreen(
                cartItems = cartItems,
                authViewModel = authViewModel,
                deliveryType = deliveryType,
                deliveryAddress = deliveryAddress,
                onConfirm = { realOrderId ->
                    lastOrderId = realOrderId
                    cartItems = emptyList()
                    deliveryAddress = "" // Reset
                    navController.navigate(Screen.OrderTracking.buildRoute(realOrderId)) {
                        popUpTo(Screen.CustomerMain.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OrderTracking.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onBack = {
                    navController.navigate(Screen.CustomerMain.buildRoute("orders")) {
                        popUpTo(Screen.CustomerMain.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CustomerBooking.route) {
            BookingScreen(
                authViewModel = authViewModel,
                onConfirm = { _ -> navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
