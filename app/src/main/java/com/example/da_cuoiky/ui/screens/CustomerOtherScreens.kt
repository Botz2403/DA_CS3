package com.example.da_cuoiky.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.da_cuoiky.model.*
import com.example.da_cuoiky.ui.theme.*

// ─────────────────────────────────
// BOOKING / RESERVATION SCREEN
// ─────────────────────────────────

@Composable
fun BookingScreen(
    user: User = SampleData.customerUser,
    onConfirm: (Reservation) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0=details, 1=confirm, 2=success
    var pax by remember { mutableIntStateOf(2) }
    var selectedDate by remember { mutableStateOf("20/04/2026") }
    var selectedTime by remember { mutableStateOf("18:00") }
    var selectedZone by remember { mutableStateOf(TableZone.INDOOR) }
    var specialRequest by remember { mutableStateOf("") }
    var addDeposit by remember { mutableStateOf(false) }

    val reservation = Reservation(
        id = "R${System.currentTimeMillis()}",
        userId = user.id, userName = user.name,
        branchId = "B01",
        datetime = "$selectedTime, $selectedDate", pax = pax,
        zone = selectedZone, specialRequests = specialRequest,
        status = ReservationStatus.CONFIRMED,
        deposit = if (addDeposit) 200000 else 0,
        qrCode = "RES-GH-${System.currentTimeMillis()}"
    )

    when (step) {
        0 -> BookingDetailsForm(
            pax = pax, onPaxChange = { pax = it },
            selectedDate = selectedDate, onDateChange = { selectedDate = it },
            selectedTime = selectedTime, onTimeChange = { selectedTime = it },
            selectedZone = selectedZone, onZoneChange = { selectedZone = it },
            specialRequest = specialRequest, onSpecialRequestChange = { specialRequest = it },
            addDeposit = addDeposit, onDepositChange = { addDeposit = it },
            onBack = onBack, onNext = { step = 1 }
        )
        1 -> BookingConfirmation(
            reservation = reservation,
            onConfirm = { step = 2 },
            onBack = { step = 0 }
        )
        2 -> BookingSuccessScreen(
            reservation = reservation,
            onDone = { onConfirm(reservation) }
        )
    }
}

@Composable
private fun BookingDetailsForm(
    pax: Int, onPaxChange: (Int) -> Unit,
    selectedDate: String, onDateChange: (String) -> Unit,
    selectedTime: String, onTimeChange: (String) -> Unit,
    selectedZone: TableZone, onZoneChange: (TableZone) -> Unit,
    specialRequest: String, onSpecialRequestChange: (String) -> Unit,
    addDeposit: Boolean, onDepositChange: (Boolean) -> Unit,
    onBack: () -> Unit, onNext: () -> Unit
) {
    Scaffold(
        topBar = { CustomerTopBar(title = "Đặt Bàn Trước", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Branch info
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.08f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = PrimaryColor,
                                modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(SampleData.branch.name, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(SampleData.branch.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                // Số khách
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Số lượng khách",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { if (pax > 1) onPaxChange(pax - 1) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircle, "Giảm",
                                        modifier = Modifier.size(36.dp),
                                        tint = if (pax > 1) PrimaryColor else MaterialTheme.colorScheme.outline)
                                }
                                Text(
                                    "$pax người",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                                IconButton(
                                    onClick = { if (pax < 20) onPaxChange(pax + 1) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, "Tăng",
                                        modifier = Modifier.size(36.dp), tint = PrimaryColor)
                                }
                            }
                        }
                    }
                }

                // Date & Time
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ngày & Giờ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = selectedDate,
                                    onValueChange = onDateChange,
                                    label = { Text("Ngày") },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp), singleLine = true
                                )
                                OutlinedTextField(
                                    value = selectedTime,
                                    onValueChange = onTimeChange,
                                    label = { Text("Giờ") },
                                    leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp), singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Time quick pick
                            Text("Khung giờ phổ biến",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("11:30", "12:00", "18:00", "18:30", "19:00").forEach { t ->
                                    FilterChip(
                                        selected = selectedTime == t,
                                        onClick = { onTimeChange(t) },
                                        label = { Text(t, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryColor.copy(alpha = 0.15f),
                                            selectedLabelColor = PrimaryColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Zone
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Khu vực ngồi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TableZone.entries.forEach { zone ->
                                    FilterChip(
                                        selected = selectedZone == zone,
                                        onClick = { onZoneChange(zone) },
                                        label = { Text(zone.displayName,
                                            style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Special requests
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Yêu Cầu Đặc Biệt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = specialRequest,
                                onValueChange = onSpecialRequestChange,
                                placeholder = { Text("Ví dụ: Có trẻ em, cần ghế cao, dị ứng hải sản...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp), maxLines = 3
                            )
                        }
                    }
                }

                // Deposit
                item {
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Đặt tiền cọc",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Text("200,000 ₫ — giữ chỗ đảm bảo, hoàn tiền nếu hủy trước 2h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(checked = addDeposit, onCheckedChange = onDepositChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor,
                                    checkedTrackColor = PrimaryColor.copy(alpha = 0.3f)))
                        }
                    }
                }
            }

            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.ArrowForward, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tiếp theo: Xác nhận đặt bàn", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BookingConfirmation(
    reservation: Reservation,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { CustomerTopBar(title = "Xác Nhận Đặt Bàn", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Chi Tiết Đặt Bàn",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold)
                    HorizontalDivider()
                    BookingInfoRow(Icons.Default.TableBar, "Chi nhánh", SampleData.branch.name)
                    BookingInfoRow(Icons.Default.CalendarToday, "Ngày giờ", reservation.datetime)
                    BookingInfoRow(Icons.Default.People, "Số khách", "${reservation.pax} người")
                    BookingInfoRow(Icons.Default.LocationOn, "Khu vực", reservation.zone.displayName)
                    if (reservation.specialRequests.isNotEmpty())
                        BookingInfoRow(Icons.Default.Note, "Yêu cầu", reservation.specialRequests)
                    if (reservation.deposit > 0)
                        BookingInfoRow(Icons.Default.Payment, "Tiền cọc",
                            "%,d ₫".format(reservation.deposit))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xác Nhận Đặt Bàn", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun BookingInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, label, tint = PrimaryColor, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BookingSuccessScreen(reservation: Reservation, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1976D2), Color(0xFF0D47A1)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(80.dp))
            Text("Đặt Bàn Thành Công!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("${reservation.datetime} — ${reservation.pax} người",
                color = Color.White.copy(alpha = 0.85f))

            // QR Code Placeholder
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(180.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.QrCode2, "QR Code",
                        modifier = Modifier.size(100.dp), tint = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(reservation.qrCode.takeLast(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }

            Text("Quét QR này khi đến nhà hàng để check-in",
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Về Trang Chủ", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────
// CUSTOMER PROFILE SCREEN
// ─────────────────────────────────

@Composable
fun CustomerProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = { CustomerTopBar(title = "Hồ Sơ Cá Nhân", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Profile header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(
                            listOf(PrimaryColor, PrimaryVariant)
                        ))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White,
                                modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (user != null) {
                            Text(user.name, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(user.phone, color = Color.White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Stars, null, tint = WarningColor,
                                        modifier = Modifier.size(20.dp))
                                    Text("${user.loyaltyPoints} điểm tích lũy",
                                        color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text("Chào mừng bạn!", style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Đăng nhập để nhận ưu đãi", color = Color.White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToLogin,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Đăng Nhập", color = PrimaryColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Loyalty progress (Chỉ hiện khi đã đăng nhập)
            if (user != null) {
                item {
                    Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("🥇 Thành Viên Vàng", fontWeight = FontWeight.Bold)
                                Text("${user.loyaltyPoints} / 500 điểm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { 0.7f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = WarningColor,
                                trackColor = WarningColor.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            // Menu options
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    
                    if (user != null) {
                        ProfileMenuItem(Icons.Default.History, "Lịch sử đơn hàng", "Xem lại các đơn đã đặt")
                        ProfileMenuItem(Icons.Default.TableBar, "Đặt bàn của tôi", "Quản lý lịch hẹn")
                        ProfileMenuItem(Icons.Default.LocationOn, "Địa chỉ giao hàng", "Sửa địa chỉ mặc định")
                        ProfileMenuItem(Icons.Default.CreditCard, "Phương thức thanh toán", "Quản lý thẻ, ví")
                    }

                    ProfileMenuItem(Icons.Default.Notifications, "Thông báo", "Cập nhật khuyến mãi")
                    ProfileMenuItem(Icons.Default.Help, "Hỗ trợ & Góp ý", "Liên hệ với chúng tôi")
                    
                    if (user != null) {
                        ProfileMenuItem(
                            icon = Icons.Default.Logout, 
                            title = "Đăng xuất", 
                            subtitle = "Thoát tài khoản hiện tại", 
                            isDestructive = true,
                            onClick = onLogout
                        )
                    } else {
                        // RÚT GỌN CHỈ CÒN 1 NÚT ĐĂNG NHẬP (ĐÃ CÓ Ở HEADER) HOẶC ĐỔI TÊN Ở ĐÂY
                        ProfileMenuItem(
                            icon = Icons.Default.Login, 
                            title = "Đăng nhập", 
                            subtitle = "Khách hàng, Nhân viên hoặc Bếp",
                            onClick = onNavigateToLogin
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, title,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else PrimaryColor,
                modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface)
                if (subtitle != null)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}
