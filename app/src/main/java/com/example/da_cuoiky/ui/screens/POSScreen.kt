package com.example.da_cuoiky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.ui.theme.*

@Composable
fun POSScreen(
    order: Order = SampleData.sampleOrder,
    onPaymentComplete: (PaymentMethod) -> Unit,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var showReceipt by remember { mutableStateOf(false) }
    var tipAmount by remember { mutableIntStateOf(0) }
    var discountInput by remember { mutableStateOf("") }
    val discount = discountInput.toIntOrNull() ?: 0

    val taxAmount = ((order.subtotal - discount) * 0.08).toInt()
    val grandTotal = order.subtotal - discount + taxAmount + tipAmount

    if (showReceipt) {
        ReceiptDialog(
            order = order.copy(discount = discount),
            method = selectedMethod ?: PaymentMethod.CASH,
            tip = tipAmount, tax = taxAmount, grandTotal = grandTotal,
            onDismiss = {
                showReceipt = false
                onPaymentComplete(selectedMethod ?: PaymentMethod.CASH)
            }
        )
    }

    Scaffold(
        topBar = {
            StaffTopBar(
                title = "Thu Ngân — ${order.tableName}",
                subtitle = "Order #${order.id}",
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // ── Left: Order Summary ───────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Chi Tiết Đơn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(order.items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${item.qty}× ${item.name}",
                                    style = MaterialTheme.typography.bodyMedium)
                                if (item.note.isNotBlank())
                                    Text("* ${item.note}", color = MaterialTheme.colorScheme.outline,
                                        style = MaterialTheme.typography.labelSmall)
                            }
                            Text("%,d ₫".format(item.totalPrice),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }

                // Totals card
                Card(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        POSTotalRow("Tạm tính", "%,d ₫".format(order.subtotal))

                        // Discount input
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Giảm giá:", modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = { discountInput = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("0") },
                                suffix = { Text("₫") },
                                modifier = Modifier.width(130.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        POSTotalRow("Thuế (8%)", "%,d ₫".format(taxAmount))
                        POSTotalRow("Tip", "%,d ₫".format(tipAmount))
                        HorizontalDivider()
                        POSTotalRow(
                            "TỔNG CỘNG", "%,d ₫".format(grandTotal),
                            style = MaterialTheme.typography.titleLarge,
                            valueColor = PrimaryColor
                        )
                    }
                }

                // Tip buttons
                Text("  Tip nhanh:", style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 12.dp))
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 10000, 20000, 50000).forEach { tip ->
                        OutlinedButton(
                            onClick = { tipAmount = tip },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (tipAmount == tip) PrimaryColor else MaterialTheme.colorScheme.outline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (tipAmount == tip) PrimaryColor else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (tip == 0) "Không" else "%,d ₫".format(tip),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // ── Right: Payment Selection ──────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(
                    "Phương Thức Thanh Toán",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentMethod.entries.forEach { method ->
                        PaymentMethodCard(
                            method = method,
                            isSelected = selectedMethod == method,
                            onClick = { selectedMethod = method }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Confirm Payment
                Button(
                    onClick = { showReceipt = true },
                    enabled = selectedMethod != null,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("XÁC NHẬN THANH TOÁN", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "%,d ₫".format(grandTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun POSTotalRow(
    label: String,
    value: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style)
        Text(value, style = style, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.08f)
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(method.icon, fontSize = 24.sp)
            Text(
                method.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, "Đã chọn", tint = PrimaryColor)
            }
        }
    }
}

@Composable
private fun ReceiptDialog(
    order: Order,
    method: PaymentMethod,
    tip: Int,
    tax: Int,
    grandTotal: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Receipt, null,
                    modifier = Modifier.size(48.dp), tint = SuccessColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Thanh Toán Thành Công!",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    color = SuccessColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Phương thức: ${method.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.qty}× ${item.name}", style = MaterialTheme.typography.bodySmall)
                        Text("%,d ₫".format(item.totalPrice), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                if (order.discount > 0)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Giảm giá"); Text("-%,d ₫".format(order.discount), color = SuccessColor)
                    }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Thuế (8%)"); Text("%,d ₫".format(tax))
                }
                if (tip > 0)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Tip"); Text("%,d ₫".format(tip))
                    }
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("TỔNG CỘNG", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text("%,d ₫".format(grandTotal), fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    Text("ĐÓNG & IN HOÁ ĐƠN")
                }
            }
        }
    }
}
