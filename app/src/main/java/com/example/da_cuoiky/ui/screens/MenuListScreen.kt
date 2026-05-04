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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.ui.theme.*
import com.example.da_cuoiky.ui.viewmodel.MenuViewModel

@Composable
fun MenuListScreen(
    viewModel: MenuViewModel = viewModel(),
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onBack: () -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var cartCount by remember { mutableIntStateOf(0) }

    // SỬA LỖI: Kiểm tra null an toàn khi lọc
    val filteredItems = menuItems.filter { item ->
        val itemName = item.name ?: ""
        val itemCatId = item.categoryId.toString()

        val matchCategory = selectedCategory == "Tất cả" || itemCatId == selectedCategory
        val matchSearch = itemName.contains(searchQuery, ignoreCase = true)
        matchCategory && matchSearch
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Lỗi: $error", color = Color.Red, textAlign = TextAlign.Center)
                    Button(onClick = { viewModel.fetchMenu() }) { Text("Thử lại") }
                }
            } else if (menuItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Không có dữ liệu món ăn", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Tìm kiếm món ăn...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(14.dp), singleLine = true
                        )
                    }

                    item {
                        val categories = listOf("Tất cả") + menuItems.map { it.categoryId.toString() }.distinct()
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }

                    items(filteredItems.chunked(2)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { item ->
                                MenuGridCard(
                                    modifier = Modifier.weight(1f),
                                    item = item,
                                    onClick = { onProductClick(item.id ?: "") },
                                    onAdd = { cartCount++ }
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
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
                val fullImageUrl = if (item.imageUrl?.startsWith("http") == true) {
                    item.imageUrl
                } else {
                    "http://10.0.2.2/WEB_ADMIN/view/uploads/${item.imageUrl}"
                }

                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.name ?: "Món ăn",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // SỬA LỖI: Định dạng giá tiền an toàn
                    Text(
                        text = "%,d ₫".format(item.price ?: 0),
                        color = Color(0xFFEF6C00),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEF6C00), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
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
                    badge = { if (cartCount > 0) Badge { Text("$cartCount") } }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, "Giỏ hàng")
                    }
                }
            }
        }
    )
}

@Composable
fun ProductDetailScreen(itemId: String, onAddToCart: (OrderItem) -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Detail: $itemId") }
}
