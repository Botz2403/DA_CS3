package com.example.da_cuoiky.network

import com.example.da_cuoiky.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // Gọi đến file api_menu.php để nhận dữ liệu JSON sạch
    @GET("view/api_menu.php")
    suspend fun getMenuItems(): List<MenuItem>

    @POST("view/api_sync_user.php")
    suspend fun syncUserToMySQL(@Body request: SyncUserRequest): Response<SyncUserResponse>

    @POST("view/api_login_staff.php")
    suspend fun loginStaff(@Body request: StaffLoginRequest): Response<StaffLoginResponse>

    @GET("view/api_get_tables.php")
    suspend fun getTables(): Response<TableApiResponse>

    @POST("view/api_create_order.php")
    suspend fun createOrder(@Body order: Order): Response<GenericApiResponse>
}
