package com.example.da_cuoiky.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────
// CORE ENUMS
// ─────────────────────────────────

enum class UserRole { STAFF, CUSTOMER, KITCHEN, MANAGER }
enum class TableStatus { EMPTY, RESERVED, OCCUPIED, PAID, LOCKED }
enum class TableZone(val displayName: String) {
    INDOOR("Trong nhà"),
    OUTDOOR("Ngoài trời"),
    VIP("Phòng VIP"),
    BAR("Quầy Bar")
}

enum class OrderStatus(val displayName: String) {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    PREPARING("Đang chuẩn bị"),
    READY("Sẵn sàng"),
    DELIVERING("Đang giao"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy")
}

enum class DeliveryType(val displayName: String) {
    DINE_IN("Tại bàn"),
    PICKUP("Tự đến lấy"),
    DELIVERY("Giao tận nơi")
}

enum class PaymentMethod(val displayName: String, val icon: String) {
    CASH("Tiền mặt", "💵"),
    CARD("Thẻ tín dụng", "💳"),
    QR("Quét QR", "📱"),
    MOMO("MoMo", "🟣"),
    VNPAY("VNPay", "🔵")
}

enum class ReservationStatus { PENDING, CONFIRMED, CHECKED_IN, CANCELLED, NO_SHOW }

// ─────────────────────────────────
// USER & AUTH
// ─────────────────────────────────

data class User(
    @SerializedName("ma_nguoi_dung") val id: String,
    @SerializedName("ho_ten") val name: String,
    @SerializedName("so_dien_thoai") val phone: String,
    @SerializedName("email") val email: String = "",
    @SerializedName("vai_tro") val role: UserRole = UserRole.CUSTOMER,
    @SerializedName("anh_dai_dien") val avatarUrl: String = "",
    val loyaltyPoints: Int = 0,
    val defaultAddresses: List<String> = emptyList()
)

// ─────────────────────────────────
// BRANCH
// ─────────────────────────────────

data class Branch(
    @SerializedName("ma_chi_nhanh") val id: String,
    @SerializedName("ten_chi_nhanh") val name: String,
    @SerializedName("dia_chi") val address: String,
    @SerializedName("so_dien_thoai") val phone: String,
    @SerializedName("gio_mo_cua") val openHours: String,
    val deliveryFee: Int = 0,
    val serviceFee: Int = 0,
    val taxRate: Double = 0.08
)

// ─────────────────────────────────
// MENU
// ─────────────────────────────────

data class ModifierOption(
    val id: String,
    val name: String,
    val extraPrice: Int = 0
)

data class Modifier(
    val id: String,
    val name: String,
    val type: String, // "single" | "multi"
    val options: List<ModifierOption>
)

data class MenuItem(
    @SerializedName("ma_mon_an") val id: String,
    @SerializedName("ten_mon") val name: String,
    @SerializedName("gia_ban") val price: Int,
    @SerializedName("ma_danh_muc") val categoryId: Int,
    @SerializedName("duong_dan_anh") val imageUrl: String?,
    @SerializedName("mo_ta_ngan") val description: String? = "",
    @SerializedName("trang_thai") val status: String = "dang_ban",

    @SerializedName("ten_danh_muc") val category: String? = "Món ăn",
    val modifiers: List<Modifier> = emptyList(),
    val isAvailable: Boolean = true,
    val prepTime: Int = 10,
    val calories: Int = 0,
    val allergens: List<String> = emptyList(),
    val isPopular: Boolean = false,
    val isVegetarian: Boolean = false
)

// ─────────────────────────────────
// ORDER
// ─────────────────────────────────

data class OrderItem(
    val menuItemId: String,
    val name: String,
    val qty: Int,
    val price: Int,
    val note: String = "",
    val selectedModifiers: List<ModifierOption> = emptyList()
) {
    val totalPrice: Int get() = (price + selectedModifiers.sumOf { it.extraPrice }) * qty
}

data class Order(
    val id: String,
    val tableId: String = "",
    val tableName: String = "",
    val userId: String = "",
    val branchId: String = "B01",
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.PENDING,
    val deliveryType: DeliveryType = DeliveryType.DINE_IN,
    val paymentInfo: PaymentInfo = PaymentInfo(),
    val eta: String = "",
    val note: String = "",
    val createdAt: String = "",
    val discount: Int = 0,
    val promoCode: String = ""
) {
    val subtotal: Int get() = items.sumOf { it.totalPrice }
    val total: Int get() = subtotal - discount
}

data class PaymentInfo(
    val method: PaymentMethod = PaymentMethod.CASH,
    val status: String = "PENDING",
    val transactionId: String = "",
    val paidAt: String = ""
)

// ─────────────────────────────────
// TABLE & OTHERS
// ─────────────────────────────────

data class TableModel(
    @SerializedName("ma_ban") val id: String,
    @SerializedName("ten_ban") val name: String,
    @SerializedName("so_cho_ngoi") val capacity: Int,
    @SerializedName("trang_thai") val statusStr: String = "trong",
    @SerializedName("khu_vuc") val zoneStr: String = "Trong nhà",
    val currentOrderId: String = ""
) {
    val status: TableStatus get() = when(statusStr) {
        "co_khach" -> TableStatus.OCCUPIED
        "dat_truoc" -> TableStatus.RESERVED
        else -> TableStatus.EMPTY
    }
    val zone: TableZone get() = when(zoneStr) {
        "Phòng VIP" -> TableZone.VIP
        "Ngoài trời" -> TableZone.OUTDOOR
        "Quầy Bar" -> TableZone.BAR
        else -> TableZone.INDOOR
    }
}

data class KitchenOrder(
    val orderId: String,
    val tableName: String,
    val items: List<OrderItem>,
    val timeElapsed: String,
    val isOverdue: Boolean,
    val status: String
)

data class Reservation(
    @SerializedName("ma_dat_cho") val id: String,
    val userId: String,
    val userName: String,
    val branchId: String,
    val datetime: String,
    val pax: Int,
    val zone: TableZone = TableZone.INDOOR,
    val specialRequests: String = "",
    val status: ReservationStatus = ReservationStatus.PENDING,
    val deposit: Int = 0,
    val qrCode: String = ""
)

data class InventoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("ten_nguyen_lieu") val name: String,
    @SerializedName("don_vi_tinh") val unit: String,
    @SerializedName("so_luong_ton") val qtyOnHand: Double,
    @SerializedName("nguong_bao_dong") val lowStockThreshold: Double,
    @SerializedName("gia_von_nhap") val importPrice: Double,
    @SerializedName("ma_danh_muc") val categoryId: Int
) {
    val isLowStock: Boolean get() = qtyOnHand <= lowStockThreshold
}

// ─────────────────────────────────
// SAMPLE DATA REPOSITORY
// ─────────────────────────────────

object SampleData {
    val branch = Branch(
        id = "B01", name = "Nhà Hàng Gourmet Hub",
        address = "12 Lê Lợi, Q.1", phone = "028 3822 0000",
        openHours = "10:00 - 22:00"
    )

    val categories = listOf("Tất cả", "Món chính", "Khai vị", "Đồ uống", "Tráng miệng")

    val menuItems = listOf(
        MenuItem(id = "M01", name = "Phở Bò Đặc Biệt", price = 65000, categoryId = 1, imageUrl = "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400"),
        MenuItem(id = "M02", name = "Bún Bò Huế", price = 58000, categoryId = 1, imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400"),
        MenuItem(id = "M03", name = "Cơm Tấm Sườn Nướng", price = 75000, categoryId = 1, imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=400")
    )

    val tables = listOf(
        TableModel("T01", "Bàn 01", 4, "co_khach", currentOrderId = "O1001"),
        TableModel("T02", "Bàn 02", 2, "trong")
    )

    val kitchenOrders = listOf(
        KitchenOrder(
            orderId = "O1001", tableName = "Bàn 01",
            items = listOf(OrderItem("M01", "Phở Bò", 2, 65000)),
            timeElapsed = "05:00", isOverdue = false, status = "PENDING"
        )
    )

    val sampleOrder = Order(
        id = "O1001", items = listOf(OrderItem("M01", "Phở Bò Đặc Biệt", 2, 65000))
    )

    val customerUser = User(id = "U002", name = "Lan", phone = "098", role = UserRole.CUSTOMER)

    val reservations = listOf(
        Reservation(
            id = "RES01", userId = "U002", userName = "Lan", branchId = "B01",
            datetime = "20:00, 20/04/2026", pax = 4, status = ReservationStatus.CONFIRMED
        )
    )

    val inventoryItems = listOf(
        InventoryItem(1, "Bò thăn loại 1", "kg", 15.0, 3.0, 280000.0, 6),
        InventoryItem(2, "Sườn heo non", "kg", 20.0, 5.0, 180000.0, 6),
        InventoryItem(3, "Gạo tấm thơm", "kg", 50.0, 10.0, 22000.0, 5),
        InventoryItem(4, "Bánh phở tươi", "kg", 25.0, 5.0, 15000.0, 6),
        InventoryItem(9, "Sữa đặc (Lon 380g)", "lon", 48.0, 12.0, 18500.0, 5)
    )
}