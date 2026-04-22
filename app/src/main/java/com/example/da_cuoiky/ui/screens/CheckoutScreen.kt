package com.example.da_cuoiky.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.da_cuoiky.model.Order
import com.example.da_cuoiky.ui.components.OrderSummary

@Composable
fun CheckoutScreen(order: Order, onPaymentSubmit: (method: String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chọn Phương Thức Thanh Toán", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Split payment & Bill details display wrapper
        OrderSummary(order = order, onCheckout = {}) // Re-use the existing pure stateless UI component

        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onPaymentSubmit("CASH") }, modifier=Modifier.weight(1f).height(64.dp)) {
                Icon(Icons.Default.AttachMoney, contentDescription=null)
                Spacer(Modifier.width(8.dp))
                Text("Tiền mặt")
            }
            OutlinedButton(onClick = { onPaymentSubmit("CARD") }, modifier=Modifier.weight(1f).height(64.dp)) {
                Icon(Icons.Default.CreditCard, contentDescription=null)
                Spacer(Modifier.width(8.dp))
                Text("Thẻ tín dụng")
            }
            Button(onClick = { onPaymentSubmit("QR") }, modifier=Modifier.weight(1f).height(64.dp)) {
                Icon(Icons.Default.QrCodeScanner, contentDescription=null)
                Spacer(Modifier.width(8.dp))
                Text("Quét mã QR")
            }
        }
    }
}
