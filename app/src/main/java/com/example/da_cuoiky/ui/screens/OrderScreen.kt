package com.example.da_cuoiky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.ui.theme.*

@Composable
fun OrderScreen(
    tableId: String = "T01",
    tableName: String = "Bàn 01",
    onSendToKitchen: (Order) -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var cartItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var showNoteDialog by remember { mutableStateOf<String?>(null) } // menuItemId

    val filteredMenu = SampleData.menuItems.filter { item ->
        val matchCategory = selectedCategory == "Tất cả" || item.category == selectedCategory
        val matchSearch = item.name.contains(searchQuery, ignoreCase = true)
        matchCategory && matchSearch && item.isAvailable
    }

    val subtotal = cartItems.sumOf { it.totalPrice }

    Scaffold(
        topBar = {
            StaffTopBar(
                title = "Order — $tableName",
                subtitle = "${cartItems.sumOf { it.qty }} món • %,d ₫".format(subtotal),
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // ── Left: Menu Panel ──────────────────────────────────
            Column(modifier = Modifier.weight(1.6f).fillMaxHeight()) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm món...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty())
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleData.categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Menu Items
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMenu, key = { it.id }) { item ->
                        StaffMenuRow(
                            item = item,
                            qtyInCart = cartItems.find { it.menuItemId == item.id }?.qty ?: 0,
                            onAdd = {
                                cartItems = cartItems.toMutableList().also { list ->
                                    val idx = list.indexOfFirst { it.menuItemId == item.id }
                                    if (idx >= 0) list[idx] = list[idx].copy(qty = list[idx].qty + 1)
                                    else list.add(OrderItem(item.id, item.name, 1, item.price))
                                }
                            },
                            onRemove = {
                                cartItems = cartItems.toMutableList().also { list ->
                                    val idx = list.indexOfFirst { it.menuItemId == item.id }
                                    if (idx >= 0) {
                                        if (list[idx].qty > 1) list[idx] = list[idx].copy(qty = list[idx].qty - 1)
                                        else list.removeAt(idx)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp)

            // ── Right: Cart Panel ─────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Đơn hàng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCartCheckout, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Chưa có món nào", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        items(cartItems, key = { it.menuItemId }) { item ->
                            CartItemRow(item = item)
                        }
                    }
                }

                // Cart Summary & Send Button
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tạm tính", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "%,d ₫".format(subtotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val order = Order(
                                    id = "O${System.currentTimeMillis()}",
                                    tableId = tableId, tableName = tableName,
                                    items = cartItems
                                )
                                onSendToKitchen(order)
                            },
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                        ) {
                            Icon(Icons.Default.OutdoorGrill, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GỬI BẾP", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffMenuRow(
    item: MenuItem,
    qtyInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("%,d ₫".format(item.price), color = PrimaryColor,
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                if (item.prepTime > 0) {
                    Text("⏱ ${item.prepTime} phút", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
            // Qty control
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (qtyInCart > 0) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.RemoveCircle, "Giảm", tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp))
                    }
                    Text(
                        "$qtyInCart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        color = PrimaryColor
                    )
                }
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircle, "Thêm", tint = PrimaryColor,
                        modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${item.qty}× ${item.name}",
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (item.note.isNotBlank())
                Text("* ${item.note}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
        }
        Text(
            "%,d ₫".format(item.totalPrice),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(thickness = 0.5.dp)
}
