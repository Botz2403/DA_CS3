package com.example.da_cuoiky.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.da_cuoiky.fiebase.AuthViewModel
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.navigation.Screen

// ── Tab enum ──────────────────────────────────────────────────────────────────
enum class CustomerTab { HOME, MENU, BOOKING, ORDERS, PROFILE }

// ── Màn hình chính sau khi đăng nhập ─────────────────────────────────────────
@Composable
fun CustomerMainScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    initialTab: CustomerTab = CustomerTab.HOME,
    cartItems: List<OrderItem>,
    onAddToCart: (OrderItem) -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected  = selectedTab == CustomerTab.HOME,
                    onClick   = { selectedTab = CustomerTab.HOME },
                    icon      = { Icon(Icons.Default.Home, "Trang chủ") },
                    label     = { Text("Trang chủ") }
                )
                NavigationBarItem(
                    selected  = selectedTab == CustomerTab.MENU,
                    onClick   = { selectedTab = CustomerTab.MENU },
                    icon      = { Icon(Icons.Default.RestaurantMenu, "Thực đơn") },
                    label     = { Text("Thực đơn") }
                )
                NavigationBarItem(
                    selected  = selectedTab == CustomerTab.BOOKING,
                    onClick   = { selectedTab = CustomerTab.BOOKING },
                    icon      = { Icon(Icons.Default.TableBar, "Đặt bàn") },
                    label     = { Text("Đặt bàn") }
                )
                NavigationBarItem(
                    selected  = selectedTab == CustomerTab.ORDERS,
                    onClick   = { selectedTab = CustomerTab.ORDERS },
                    icon      = { Icon(Icons.Default.ReceiptLong, "Đơn hàng") },
                    label     = { Text("Đơn hàng") }
                )
                NavigationBarItem(
                    selected  = selectedTab == CustomerTab.PROFILE,
                    onClick   = { selectedTab = CustomerTab.PROFILE },
                    icon      = { Icon(Icons.Default.AccountCircle, "Hồ sơ") },
                    label     = { Text("Hồ sơ") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                CustomerTab.HOME -> CustomerHomeScreen(
                    user           = null,
                    onMenuClick    = { selectedTab = CustomerTab.MENU },
                    onBookingClick = { selectedTab = CustomerTab.BOOKING },
                    onOrdersClick  = { selectedTab = CustomerTab.ORDERS },
                    onProfileClick = { selectedTab = CustomerTab.PROFILE },
                    onProductClick = { itemId ->
                        navController.navigate(Screen.ProductDetail.buildRoute(itemId))
                    }
                )

                CustomerTab.MENU -> MenuListScreen(
                    onProductClick = { itemId ->
                        navController.navigate(Screen.ProductDetail.buildRoute(itemId))
                    },
                    onCartClick = {
                        navController.navigate(Screen.CustomerCart.route)
                    },
                    onBack = { selectedTab = CustomerTab.HOME }
                )

                CustomerTab.BOOKING -> BookingScreen(
                    user      = SampleData.customerUser,
                    onConfirm = { selectedTab = CustomerTab.HOME },
                    onBack    = { selectedTab = CustomerTab.HOME }
                )

                CustomerTab.ORDERS -> OrderTrackingScreen(
                    orderId = "O1001",
                    onBack  = { selectedTab = CustomerTab.HOME }
                )

                CustomerTab.PROFILE -> {
                    if (authViewModel.isLoggedIn()) {
                        // Đã đăng nhập → hiện hồ sơ với data Firebase thật
                        CustomerProfileScreen(
                            navController     = navController,
                            viewModel         = authViewModel,
                            onBack            = { selectedTab = CustomerTab.HOME },
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route)
                            },
                            onLogout = {
                                navController.navigate(Screen.CustomerMain.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    } else {
                        // Chưa đăng nhập → chuyển sang LoginScreen (đầy đủ: Email, Google, Đăng ký, Quên MK)
                        navController.navigate(Screen.Login.route)
                    }
                }
            }
        }
    }
}
