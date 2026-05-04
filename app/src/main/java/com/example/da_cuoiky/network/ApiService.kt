package com.example.da_cuoiky.network

import com.example.da_cuoiky.model.*
import retrofit2.http.GET

interface ApiService {
    // Gọi đến file api_menu.php để nhận dữ liệu JSON sạch
    @GET("view/api_menu.php")
    suspend fun getMenuItems(): List<MenuItem>
}
