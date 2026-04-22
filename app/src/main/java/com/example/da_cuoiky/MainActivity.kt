package com.example.da_cuoiky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.navigation.Screen
import com.example.da_cuoiky.ui.screens.*
import com.example.da_cuoiky.ui.theme.DA_CuoiKyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    val navController = rememberNavController()
    // currentUser = null nghĩa là chưa đăng nhập (khách vãng lai)
    var currentUser by remember { mutableStateOf<User?>(null) }
    var cartItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = Screen.CustomerHome.route
    ) {
        // ─────────────────────────────────────────
        // AUTH
        // ─────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(onRoleSelected = { role ->
                // Giả lập tạo User object sau khi đăng nhập thành công
                // Trong thực tế, bạn sẽ lấy User từ Firestore dựa trên UID
                val mockUser = User(
                    id = "U${System.currentTimeMillis()}",
                    name = if(role == UserRole.CUSTOMER) "Khách Hàng Mới" else "Nhân Viên",
                    phone = "09xxx",
                    role = role
                )
                currentUser = mockUser

                when (role) {
                    UserRole.STAFF, UserRole.MANAGER ->
                        navController.navigate(Screen.StaffFloorPlan.route) {
                            popUpTo(Screen.CustomerHome.route) { inclusive = true }
                        }
                    UserRole.KITCHEN ->
                        navController.navigate(Screen.StaffKitchen.route) {
                            popUpTo(Screen.CustomerHome.route) { inclusive = true }
                        }
                    UserRole.CUSTOMER ->
                        navController.navigate(Screen.CustomerProfile.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                }
            })
        }

        // ─────────────────────────────────────────
        // STAFF FLOW
        // ─────────────────────────────────────────
        composable(Screen.StaffFloorPlan.route) {
            FloorPlanScreen(
                onTableClick = { table ->
                    if (table.status == TableStatus.OCCUPIED && table.currentOrderId.isNotEmpty()) {
                        navController.navigate(Screen.StaffPOS.buildRoute(table.currentOrderId))
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
                    navController.navigate(Screen.StaffKitchen.route)
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
        composable(Screen.CustomerHome.route) {
            CustomerHomeScreen(
                onMenuClick = { navController.navigate(Screen.CustomerMenu.route) },
                onBookingClick = { navController.navigate(Screen.CustomerBooking.route) },
                onOrdersClick = { navController.navigate(Screen.OrderTracking.buildRoute("O1001")) },
                onProfileClick = { navController.navigate(Screen.CustomerProfile.route) },
                onProductClick = { itemId -> navController.navigate(Screen.ProductDetail.buildRoute(itemId)) }
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
                user = currentUser, // Truyền User hiện tại (có thể là null)
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onLogout = { currentUser = null }
            )
        }
    }
}
