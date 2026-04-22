package com.example.da_cuoiky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.ui.theme.*

// ─────────────────────────────────
// MENU LIST SCREEN
// ─────────────────────────────────

@Composable
fun MenuListScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("popular") } // popular | price_asc | price_desc | time
    var filterVeg by remember { mutableStateOf(false) }
    var cartCount by remember { mutableIntStateOf(0) }

    val filteredItems = SampleData.menuItems.filter { item ->
        val matchCategory = selectedCategory == "Tất cả" || item.category == selectedCategory
        val matchSearch = item.name.contains(searchQuery, ignoreCase = true)
        val matchVeg = if (filterVeg) item.isVegetarian else true
        matchCategory && matchSearch && matchVeg && item.isAvailable
    }.let { list ->
        when (sortBy) {
            "price_asc" -> list.sortedBy { it.price }
            "price_desc" -> list.sortedByDescending { it.price }
            "time" -> list.sortedBy { it.prepTime }
            else -> list.sortedByDescending { it.isPopular }
        }
    }

    Scaffold(
        topBar = {
            CustomerTopBar(
                title = "Thực Đơn",
                onBack = onBack,
                cartCount = cartCount,
                onCartClick = onCartClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Search
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm món ăn...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty())
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp), singleLine = true
                )
            }

            // Category chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleData.categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryColor.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryColor
                            )
                        )
                    }
                }
            }

            // Sort & Filter row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${filteredItems.size} món", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = filterVeg,
                        onClick = { filterVeg = !filterVeg },
                        leadingIcon = if (filterVeg) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        label = { Text("🌱 Chay") }
                    )
                    SortDropdown(sortBy = sortBy, onSortChange = { sortBy = it })
                }
            }

            // Menu items grid
            items(filteredItems.chunked(2), key = { it[0].id }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { item ->
                        MenuGridCard(
                            modifier = Modifier.weight(1f),
                            item = item,
                            onClick = { onProductClick(item.id) },
                            onAdd = { cartCount++ }
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SortDropdown(sortBy: String, onSortChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val sortLabels = mapOf(
        "popular" to "📈 Phổ biến",
        "price_asc" to "💰 Giá tăng",
        "price_desc" to "💰 Giá giảm",
        "time" to "⏱ Thời gian"
    )
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(sortLabels[sortBy] ?: "Sắp xếp",
                style = MaterialTheme.typography.labelSmall)
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sortLabels.forEach { (k, v) ->
                DropdownMenuItem(
                    text = { Text(v) },
                    onClick = { onSortChange(k); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun MenuGridCard(
    modifier: Modifier = Modifier,
    item: MenuItem,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                if (!item.isAvailable) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Hết món", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                if (item.isVegetarian) {
                    Surface(
                        color = SuccessColor,
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌱", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.allergens.isNotEmpty()) {
                    Text("⚠ ${item.allergens.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("%,d ₫".format(item.price),
                            color = PrimaryColor, fontWeight = FontWeight.ExtraBold)
                        Text("⏱ ${item.prepTime}p",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(34.dp).background(PrimaryColor, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, "Thêm ${item.name}",
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────
// PRODUCT DETAIL SCREEN
// ─────────────────────────────────

@Composable
fun ProductDetailScreen(
    itemId: String = "M01",
    onAddToCart: (OrderItem) -> Unit,
    onBack: () -> Unit
) {
    val item = SampleData.menuItems.find { it.id == itemId } ?: SampleData.menuItems.first()
    var qty by remember { mutableIntStateOf(1) }
    var note by remember { mutableStateOf("") }
    var selectedModifiers by remember { mutableStateOf<Map<String, ModifierOption>>(emptyMap()) }

    val totalPrice = (item.price + selectedModifiers.values.sumOf { it.extraPrice }) * qty

    Scaffold(
        topBar = {
            CustomerTopBar(
                title = item.name,
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(shadowElevation = 12.dp) {
                Button(
                    onClick = {
                        onAddToCart(OrderItem(
                            menuItemId = item.id,
                            name = item.name,
                            qty = qty,
                            price = item.price,
                            note = note,
                            selectedModifiers = selectedModifiers.values.toList()
                        ))
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm vào giỏ — %,d ₫".format(totalPrice),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Hero image
            item {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("%,d ₫".format(item.price),
                                style = MaterialTheme.typography.titleLarge,
                                color = PrimaryColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoBadge("⏱ ${item.prepTime} phút", Color(0xFF1976D2))
                        if (item.calories > 0)
                            InfoBadge("🔥 ${item.calories} kcal", Color(0xFFE65100))
                        if (item.isVegetarian)
                            InfoBadge("🌱 Chay", SuccessColor)
                        if (item.isPopular)
                            InfoBadge("⭐ Nổi bật", WarningColor)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                    if (item.allergens.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⚠ Allergen: ${item.allergens.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Modifiers
            if (item.modifiers.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                items(item.modifiers) { modifier ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(modifier.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(if (modifier.type == "single") "Chọn 1 tùy chọn"
                             else "Chọn nhiều tùy chọn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        modifier.options.forEach { option ->
                            val isSelected = selectedModifiers[modifier.id]?.id == option.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedModifiers = selectedModifiers.toMutableMap().also {
                                            if (isSelected) it.remove(modifier.id)
                                            else it[modifier.id] = option
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = {
                                    selectedModifiers = selectedModifiers.toMutableMap().also {
                                        it[modifier.id] = option
                                    }
                                })
                                Text(option.name, modifier = Modifier.weight(1f))
                                if (option.extraPrice != 0) {
                                    Text(
                                        "%+,d ₫".format(option.extraPrice),
                                        color = if (option.extraPrice > 0) PrimaryColor else SuccessColor,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Note
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ghi chú", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Ví dụ: Ít hành, không cay...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }
            }

            // Qty selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (qty > 1) qty-- },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircle, "Giảm",
                            tint = if (qty > 1) PrimaryColor else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp))
                    }
                    Text("$qty",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 24.dp))
                    IconButton(
                        onClick = { qty++ },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, "Tăng",
                            tint = PrimaryColor, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(80.dp)) // space for bottom button
            }
        }
    }
}

@Composable
private fun InfoBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    cartCount: Int = 0,
    onCartClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Quay lại")
                }
            }
        },
        actions = {
            if (onCartClick != null) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) Badge { Text("$cartCount") }
                    }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, "Giỏ hàng")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
