package com.example.da_cuoiky.model

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
    val id: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val role: UserRole,
    val avatarUrl: String = "",
    val loyaltyPoints: Int = 0,
    val defaultAddresses: List<String> = emptyList()
)

// ─────────────────────────────────
// BRANCH
// ─────────────────────────────────

data class Branch(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val openHours: String,
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
    val id: String,
    val name: String,
    val price: Int,
    val category: String,
    val imageUrl: String,
    val description: String = "",
    val modifiers: List<Modifier> = emptyList(),
    val isAvailable: Boolean = true,
    val prepTime: Int = 10, // phút
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

data class PaymentInfo(
    val method: PaymentMethod = PaymentMethod.CASH,
    val status: String = "PENDING", // "PENDING" | "PAID" | "FAILED"
    val transactionId: String = "",
    val paidAt: String = ""
)

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

// ─────────────────────────────────
// TABLE
// ─────────────────────────────────

data class TableModel(
    val id: String,
    val name: String,
    val capacity: Int,
    val status: TableStatus = TableStatus.EMPTY,
    val zone: TableZone = TableZone.INDOOR,
    val currentOrderId: String = ""
)

// ─────────────────────────────────
// KITCHEN
// ─────────────────────────────────

data class KitchenOrder(
    val orderId: String,
    val tableName: String,
    val items: List<OrderItem>,
    val timeElapsed: String,
    val isOverdue: Boolean,
    val status: String // "PENDING", "PREPARING", "READY"
)

// ─────────────────────────────────
// RESERVATION
// ─────────────────────────────────

data class Reservation(
    val id: String,
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

// ─────────────────────────────────
// INVENTORY
// ─────────────────────────────────

data class InventoryLink(
    val menuItemId: String,
    val qtyPerServing: Double
)

data class InventoryItem(
    val id: String,
    val name: String,
    val unit: String,
    val qtyOnHand: Double,
    val lowStockThreshold: Double,
    val linkedMenuItems: List<InventoryLink> = emptyList()
) {
    val isLowStock: Boolean get() = qtyOnHand <= lowStockThreshold
}

// ─────────────────────────────────
// SAMPLE DATA REPOSITORY
// ─────────────────────────────────

object SampleData {

    val staffUser = User(
        id = "U001", name = "Trần Văn An", phone = "0901234567",
        role = UserRole.STAFF, avatarUrl = ""
    )

    val customerUser = User(
        id = "U002", name = "Nguyễn Thị Lan", phone = "0987654321",
        role = UserRole.CUSTOMER, loyaltyPoints = 350,
        defaultAddresses = listOf("123 Nguyễn Văn Linh, Q.7, TP.HCM")
    )

    val branch = Branch(
        id = "B01", name = "Nhà Hàng Gourmet Hub - Q.1",
        address = "12 Lê Lợi, Q.1, TP.HCM", phone = "028 3822 0000",
        openHours = "10:00 - 22:00", deliveryFee = 20000, taxRate = 0.08
    )

    val sizeModifier = Modifier(
        id = "MOD01", name = "Kích thước", type = "single",
        options = listOf(
            ModifierOption("O1", "Nhỏ", -10000),
            ModifierOption("O2", "Vừa", 0),
            ModifierOption("O3", "Lớn", 15000)
        )
    )

    val spiceModifier = Modifier(
        id = "MOD02", name = "Độ cay", type = "single",
        options = listOf(
            ModifierOption("O4", "Không cay", 0),
            ModifierOption("O5", "Cay vừa", 0),
            ModifierOption("O6", "Cay nhiều", 0)
        )
    )

    val menuItems = listOf(
        MenuItem(
            id = "M01", name = "Phở Bò Đặc Biệt", price = 65000,
            category = "Món chính",
            imageUrl = "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400",
            description = "Phở bò tái chín với nước dùng hầm 12 tiếng, thơm ngon đậm đà.",
            modifiers = listOf(sizeModifier, spiceModifier),
            prepTime = 12, calories = 450, isPopular = true,
            allergens = listOf("gluten")
        ),
        MenuItem(
            id = "M02", name = "Bún Bò Huế", price = 58000,
            category = "Món chính",
            imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400",
            description = "Bún bò cay đặc trưng Huế với chả lụa và huyết.",
            modifiers = listOf(spiceModifier),
            prepTime = 10, calories = 520, isPopular = true
        ),
        MenuItem(
            id = "M03", name = "Cơm Tấm Sườn Nướng", price = 75000,
            category = "Món chính",
            imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=400",
            description = "Cơm tấm với sườn nướng, bì, chả, trứng ốp la.",
            prepTime = 15, calories = 680, isPopular = true
        ),
        MenuItem(
            id = "M04", name = "Chả Giò Rán", price = 45000,
            category = "Khai vị",
            imageUrl = "https://images.unsplash.com/photo-1562802378-063ec186a863?w=400",
            description = "Chả giò giòn rụm nhân thịt & rau củ.",
            prepTime = 8, calories = 320
        ),
        MenuItem(
            id = "M05", name = "Gỏi Cuốn Tôm Thịt", price = 38000,
            category = "Khai vị",
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400",
            description = "Gỏi cuốn tươi với tôm, thịt heo, rau sống và bún.",
            prepTime = 5, isVegetarian = false, calories = 180
        ),
        MenuItem(
            id = "M06", name = "Trà Đá Chanh", price = 15000,
            category = "Đồ uống",
            imageUrl = "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400",
            description = "Trà đá chanh tươi mát lạnh.",
            prepTime = 2, calories = 45
        ),
        MenuItem(
            id = "M07", name = "Cà Phê Sữa Đá", price = 25000,
            category = "Đồ uống",
            imageUrl = "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400",
            description = "Cà phê phin truyền thống với sữa đặc.",
            prepTime = 5, calories = 120, isPopular = true
        ),
        MenuItem(
            id = "M08", name = "Bánh Flan Caramel", price = 32000,
            category = "Tráng miệng",
            imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400",
            description = "Bánh flan mịn với caramel thơm.",
            prepTime = 3, calories = 210, isVegetarian = true
        )
    )

    val categories = listOf("Tất cả", "Món chính", "Khai vị", "Đồ uống", "Tráng miệng")

    val tables = listOf(
        TableModel("T01", "Bàn 01", 4, TableStatus.OCCUPIED, TableZone.INDOOR, "O1001"),
        TableModel("T02", "Bàn 02", 2, TableStatus.EMPTY, TableZone.INDOOR),
        TableModel("T03", "Bàn 03", 6, TableStatus.RESERVED, TableZone.INDOOR),
        TableModel("T04", "Bàn 04", 4, TableStatus.EMPTY, TableZone.INDOOR),
        TableModel("T05", "Bàn 05", 4, TableStatus.PAID, TableZone.INDOOR),
        TableModel("T06", "Bàn 06", 8, TableStatus.OCCUPIED, TableZone.OUTDOOR, "O1002"),
        TableModel("T07", "Bàn 07", 4, TableStatus.EMPTY, TableZone.OUTDOOR),
        TableModel("T08", "Bàn 08", 4, TableStatus.EMPTY, TableZone.OUTDOOR),
        TableModel("T09", "VIP 01", 10, TableStatus.OCCUPIED, TableZone.VIP, "O1003"),
        TableModel("T10", "VIP 02", 6, TableStatus.EMPTY, TableZone.VIP),
        TableModel("T11", "Bar 01", 2, TableStatus.OCCUPIED, TableZone.BAR, "O1004"),
        TableModel("T12", "Bar 02", 2, TableStatus.LOCKED, TableZone.BAR)
    )

    val sampleOrder = Order(
        id = "O1001", tableId = "T01", tableName = "Bàn 01", userId = "U002",
        items = listOf(
            OrderItem("M01", "Phở Bò Đặc Biệt", 2, 65000, "Ít hành"),
            OrderItem("M07", "Cà Phê Sữa Đá", 2, 25000)
        ),
        status = OrderStatus.PREPARING, deliveryType = DeliveryType.DINE_IN,
        eta = "18:35", createdAt = "18:20"
    )

    val kitchenOrders = listOf(
        KitchenOrder("O1001", "Bàn 01",
            listOf(
                OrderItem("M01", "Phở Bò Đặc Biệt", 2, 65000, "Ít hành"),
                OrderItem("M05", "Gỏi Cuốn Tôm Thịt", 1, 38000)
            ), "08:45", true, "PREPARING"
        ),
        KitchenOrder("O1002", "Bàn 06",
            listOf(
                OrderItem("M03", "Cơm Tấm Sườn Nướng", 3, 75000),
                OrderItem("M04", "Chả Giò Rán", 2, 45000)
            ), "03:20", false, "PENDING"
        ),
        KitchenOrder("O1003", "VIP 01",
            listOf(
                OrderItem("M02", "Bún Bò Huế", 4, 58000, "Cay vừa"),
                OrderItem("M08", "Bánh Flan Caramel", 4, 32000)
            ), "01:10", false, "PENDING"
        )
    )

    val reservations = listOf(
        Reservation(
            id = "R001", userId = "U002", userName = "Nguyễn Thị Lan",
            branchId = "B01", datetime = "20:00, 20/04/2026", pax = 4,
            zone = TableZone.VIP, specialRequests = "Có trẻ em, cần ghế cao",
            status = ReservationStatus.CONFIRMED, deposit = 200000,
            qrCode = "RES-R001-20260420"
        ),
        Reservation(
            id = "R002", userId = "U003", userName = "Lê Quang Hùng",
            branchId = "B01", datetime = "19:00, 21/04/2026", pax = 2,
            zone = TableZone.OUTDOOR, status = ReservationStatus.PENDING
        )
    )

    val inventoryItems = listOf(
        InventoryItem("INV01", "Thịt bò", "kg", 15.5, 5.0,
            listOf(InventoryLink("M01", 0.15))),
        InventoryItem("INV02", "Bánh phở", "kg", 8.0, 3.0,
            listOf(InventoryLink("M01", 0.2), InventoryLink("M02", 0.2))),
        InventoryItem("INV03", "Thịt heo", "kg", 2.0, 5.0,
            listOf(InventoryLink("M03", 0.25), InventoryLink("M05", 0.08))),
        InventoryItem("INV04", "Cà phê hạt", "kg", 4.5, 1.0,
            listOf(InventoryLink("M07", 0.018))),
        InventoryItem("INV05", "Rau sống hỗn hợp", "kg", 12.0, 3.0, emptyList())
    )
}
