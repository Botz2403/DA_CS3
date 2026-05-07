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
        
        // ── In Key Hash ra Logcat cho Facebook ──
        try {
            val info = packageManager.getPackageInfo(
                "com.example.da_cuoiky",
                android.content.pm.PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = java.security.MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT)
                android.util.Log.d("FACEBOOK_KEY_HASH", keyHash)
            }
        } catch (e: Exception) { }
        // ────────────────────────────────────────

        setContent {
            DA_CuoiKyTheme {
                RestaurantApp()
            }
        }
    }
}

@Composable
fun RestaurantApp() {
    // Khởi tạo AuthViewModel dùng chung toàn app
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var cartItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }

    // Luôn vào giao diện chính — khách vãng lai có thể xem, login ở tab Hồ sơ
    val startDestination = Screen.CustomerMain.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ─────────────────────────────────────────
        // AUTH
        // ─────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                navController        = navController,
                viewModel            = authViewModel,
                onRoleSelected       = { /* chưa dùng phân quyền */ },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
            // LoginScreen tự navigate về Screen.CustomerMain sau khi login thành công
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    // Sau đăng ký thành công → về Login để đăng nhập
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ─────────────────────────────────────────
        // STAFF FLOW
        // ─────────────────────────────────────────
        composable(Screen.StaffFloorPlan.route) {
            FloorPlanScreen(
                onTableClick = { table ->
                    if (table.status == TableStatus.OCCUPIED) {
                        val targetId = if (table.currentOrderId.isNotEmpty()) table.currentOrderId else "POS_${table.id}"
                        navController.navigate(Screen.StaffPOS.buildRoute(targetId))
                    }
                },
                onNavigateToOrder = { tableId ->
                    navController.navigate(Screen.StaffOrder.buildRoute(tableId))
                }
            )
        }

        composable(Screen.StaffOrder.route) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getString("tableId") ?: "T01"
            val table = SampleData.tables.find { it.id == tableId }
            OrderScreen(
                tableId = tableId,
                tableName = table?.name ?: "Bàn",
                onSendToKitchen = {
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StaffKitchen.route) {
            KitchenScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StaffPOS.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: "O1001"
            POSScreen(
                order = SampleData.sampleOrder.copy(id = orderId),
                onPaymentComplete = { _ ->
                    navController.navigate(Screen.StaffFloorPlan.route) {
                        popUpTo(Screen.StaffFloorPlan.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─────────────────────────────────────────
        // CUSTOMER FLOW
        // ─────────────────────────────────────────
        // ── Màn chính có Bottom Navigation ──────────────────────────
        composable(Screen.CustomerMain.route) {
            CustomerMainScreen(
                navController  = navController,
                authViewModel  = authViewModel,
                initialTab     = CustomerTab.HOME,   // ← mở tab Trang chủ sau login
                cartItems      = cartItems,
                onAddToCart    = { item -> cartItems = cartItems + item }
            )
        }

        composable(Screen.CustomerHome.route) {
            CustomerMainScreen(
                navController  = navController,
                authViewModel  = authViewModel,
                initialTab     = CustomerTab.HOME,
                cartItems      = cartItems,
                onAddToCart    = { item -> cartItems = cartItems + item }
            )
        }

        composable(Screen.CustomerMenu.route) {
            MenuListScreen(
                onProductClick = { itemId -> navController.navigate(Screen.ProductDetail.buildRoute(itemId)) },
                onCartClick = { navController.navigate(Screen.CustomerCart.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProductDetail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: "M01"
            ProductDetailScreen(
                itemId = itemId,
                onAddToCart = { item ->
                    cartItems = cartItems + item
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerCart.route) {
            CartScreen(
                cartItems = cartItems,
                onCheckout = { navController.navigate(Screen.CustomerCheckout.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerCheckout.route) {
            CustomerCheckoutScreen(
                onConfirm = { _ ->
                    navController.navigate(Screen.OrderTracking.buildRoute("O${System.currentTimeMillis()}")) {
                        popUpTo(Screen.CustomerHome.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerBooking.route) {
            BookingScreen(
                user = currentUser ?: SampleData.customerUser,
                onConfirm = { _ ->
                    navController.navigate(Screen.CustomerHome.route) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OrderTracking.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: "O1001"
            OrderTrackingScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerProfile.route) {
            CustomerProfileScreen(
                navController     = navController,
                viewModel         = authViewModel,
                onBack            = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onLogout          = {
                    // Firebase logout đã được gọi trong ProfileScreen
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }  // xóa toàn bộ back stack
                    }
                }
            )
        }
    }
}
