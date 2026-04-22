package com.example.da_cuoiky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.ui.theme.*

// ─────────────────────────────────
// CART SCREEN
// ─────────────────────────────────

@Composable
fun CartScreen(
    cartItems: List<OrderItem> = SampleData.sampleOrder.items,
    onCheckout: () -> Unit,
    onBack: () -> Unit
) {
    var deliveryType by remember { mutableStateOf(DeliveryType.DINE_IN) }
    var promoCode by remember { mutableStateOf("") }
    var promoApplied by remember { mutableStateOf(false) }
    var localCart by remember { mutableStateOf(cartItems.toMutableList()) }

    val discount = if (promoApplied) (localCart.sumOf { it.totalPrice } * 0.20).toInt() else 0
    val subtotal = localCart.sumOf { it.totalPrice }
    val deliveryFee = if (deliveryType == DeliveryType.DELIVERY) 20000 else 0
    val total = subtotal - discount + deliveryFee

    Scaffold(
        topBar = {
            CustomerTopBar(title = "Giỏ Hàng (${localCart.size})", onBack = onBack)
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delivery Type
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Hình thức nhận",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DeliveryType.entries.forEach { type ->
                                    FilterChip(
                                        selected = deliveryType == type,
                                        onClick = { deliveryType = type },
                                        label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryColor.copy(alpha = 0.15f),
                                            selectedLabelColor = PrimaryColor
                                        )
                                    )
                                }
                            }
                            if (deliveryType == DeliveryType.DELIVERY) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = SampleData.customerUser.defaultAddresses.firstOrNull() ?: "",
                                    onValueChange = {},
                                    label = { Text("Địa chỉ giao hàng") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    readOnly = true,
                                    trailingIcon = { Icon(Icons.Default.Edit, "Đổi địa chỉ") }
                                )
                            }
                        }
                    }
                }

                // Cart Items
                item {
                    Text("Món đã chọn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                items(localCart, key = { it.menuItemId }) { item ->
                    CartScreen_ItemRow(
                        item = item,
                        onQtyChange = { newQty ->
                            localCart = localCart.map {
                                if (it.menuItemId == item.menuItemId) it.copy(qty = newQty) else it
                            }.filter { it.qty > 0 }.toMutableList()
                        }
                    )
                }

                // Promo Code
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mã giảm giá",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = promoCode,
                                    onValueChange = { promoCode = it.uppercase(); promoApplied = false },
                                    placeholder = { Text("Nhập mã (VD: GOURMET20)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Redeem, null) }
                                )
                                Button(
                                    onClick = { promoApplied = promoCode == "GOURMET20" },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(56.dp)
                                ) { Text("Áp dụng") }
                            }
                            if (promoApplied) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✅ Giảm 20% đã được áp dụng!",
                                    color = SuccessColor, style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Order summary
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CartTotalRow("Tạm tính", "%,d ₫".format(subtotal))
                            if (discount > 0)
                                CartTotalRow("Giảm giá", "-%,d ₫".format(discount), valueColor = SuccessColor)
                            if (deliveryFee > 0)
                                CartTotalRow("Phí giao hàng", "%,d ₫".format(deliveryFee))
                            HorizontalDivider()
                            CartTotalRow(
                                "Tổng cộng", "%,d ₫".format(total),
                                valueColor = PrimaryColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Checkout CTA
            Surface(shadowElevation = 12.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onCheckout,
                        enabled = localCart.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Payment, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tiến hành thanh toán — %,d ₫".format(total),
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartScreen_ItemRow(item: OrderItem, onQtyChange: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold)
                if (item.note.isNotBlank())
                    Text("* ${item.note}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                Text("%,d ₫".format(item.price), color = PrimaryColor,
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onQtyChange(item.qty - 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(if (item.qty > 1) Icons.Default.Remove else Icons.Default.Delete,
                        "Giảm", tint = if (item.qty > 1) MaterialTheme.colorScheme.onSurface
                                       else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp))
                }
                Text("${item.qty}", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 24.dp))
                IconButton(onClick = { onQtyChange(item.qty + 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "Tăng", tint = PrimaryColor,
                        modifier = Modifier.size(20.dp))
                }
            }
            Text("%,d".format(item.totalPrice),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.widthIn(min = 70.dp),
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CartTotalRow(
    label: String, value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style)
        Text(value, style = style, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// ─────────────────────────────────
// CUSTOMER CHECKOUT SCREEN
// ─────────────────────────────────

@Composable
fun CustomerCheckoutScreen(
    order: Order = SampleData.sampleOrder,
    onConfirm: (PaymentMethod) -> Unit,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.MOMO) }
    var confirmed by remember { mutableStateOf(false) }

    if (confirmed) {
        OrderConfirmedOverlay(
            orderId = order.id,
            eta = "~25 phút",
            onDone = { onConfirm(selectedMethod) }
        )
        return
    }

    Scaffold(
        topBar = { CustomerTopBar(title = "Thanh Toán", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Order summary
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tóm Tắt Đơn",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            order.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.qty}× ${item.name}", style = MaterialTheme.typography.bodySmall)
                                    Text("%,d ₫".format(item.totalPrice), style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng cộng", fontWeight = FontWeight.Bold)
                                Text("%,d ₫".format(order.total), color = PrimaryColor, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Payment methods
                item {
                    Text("Phương Thức Thanh Toán",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                items(PaymentMethod.entries) { method ->
                    CustomerPaymentCard(
                        method = method,
                        isSelected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                }

                // ETA
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                    ) {
                        Row(modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Schedule, null, tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(32.dp))
                            Column {
                                Text("Thời gian dự kiến",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF7B1FA2))
                                Text("~ 25 phút",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A148C))
                            }
                        }
                    }
                }
            }

            Surface(shadowElevation = 12.dp) {
                Button(
                    onClick = { confirmed = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xác Nhận Thanh Toán qua ${selectedMethod.displayName}",
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CustomerPaymentCard(method: PaymentMethod, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryColor) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.06f)
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(method.icon, fontSize = 26.sp)
            Text(method.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f))
            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = PrimaryColor)
        }
    }
}

@Composable
private fun OrderConfirmedOverlay(orderId: String, eta: String, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SuccessColor, Color(0xFF1B5E20)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(80.dp))
            Text("Đặt Hàng Thành Công!", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Mã đơn: #$orderId", color = Color.White.copy(alpha = 0.9f))
            Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Thời gian dự kiến", color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall)
                    Text(eta, style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.7f).height(52.dp)
            ) {
                Text("Theo dõi đơn hàng", color = SuccessColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────
// ORDER TRACKING SCREEN
// ─────────────────────────────────

@Composable
fun OrderTrackingScreen(
    orderId: String = "O1001",
    onBack: () -> Unit
) {
    val order = SampleData.sampleOrder.copy(id = orderId)
    var currentStep by remember { mutableIntStateOf(2) } // 0-4

    val steps = listOf(
        "Đơn đã đặt" to "18:20",
        "Nhà hàng xác nhận" to "18:22",
        "Đang chuẩn bị" to "18:25",
        "Sẵn sàng" to "--:--",
        "Hoàn thành" to "--:--"
    )

    Scaffold(
        topBar = { CustomerTopBar(title = "Theo Dõi Đơn #$orderId", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🔥", fontSize = 36.sp)
                        Column {
                            Text(order.status.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold, color = PrimaryColor)
                            Text("Dự kiến hoàn thành lúc ${order.eta}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // Timeline
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Trạng Thái Đơn",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        steps.forEachIndexed { idx, (label, time) ->
                            TrackingTimelineRow(
                                label = label,
                                time = time,
                                isDone = idx < currentStep,
                                isCurrent = idx == currentStep,
                                isLast = idx == steps.lastIndex
                            )
                        }
                    }
                }
            }

            // Order items
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Món đã đặt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        order.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.qty}× ${item.name}",
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("%,d ₫".format(item.totalPrice),
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tổng", fontWeight = FontWeight.Bold)
                            Text("%,d ₫".format(order.total),
                                color = PrimaryColor, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Contact restaurant
            item {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nhắn tin với nhà hàng")
                }
            }
        }
    }
}

@Composable
private fun TrackingTimelineRow(
    label: String, time: String,
    isDone: Boolean, isCurrent: Boolean, isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone || isCurrent -> if (isDone) SuccessColor else PrimaryColor
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone)
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                else if (isCurrent)
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(if (isDone) SuccessColor.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = if (!isLast) 16.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) PrimaryColor
                        else if (isDone) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
            )
            Text(time,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDone || isCurrent) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }
    }
}
