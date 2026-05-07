package com.example.da_cuoiky.fiebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepoesitory {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Đăng nhập ─────────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            // 1. Thử đăng nhập Firebase (dành cho Khách Hàng)
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User not found")
            syncWithPHP(user)
            
            // Trả về "role:customer" để View/ViewModel biết phân luồng
            Result.success("role:customer")
        } catch (e: Exception) {
            // 2. Firebase lỗi -> Chuyển sang kiểm tra Nhân Viên bên MySQL Web Admin
            try {
                val apiService = com.example.da_cuoiky.network.RetrofitClient.instance
                val request = com.example.da_cuoiky.model.StaffLoginRequest(email, password)
                val response = apiService.loginStaff(request)
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val staffRole = response.body()?.data?.role ?: "staff"
                    
                    // Trả về "role:staff" hoặc "role:kitchen" ...
                    Result.success("role:$staffRole")
                } else {
                    // Cả 2 hệ thống đều không đúng
                    Result.failure(Exception("Sai tài khoản hoặc mật khẩu!"))
                }
            } catch (apiError: Exception) {
                Result.failure(Exception("Lỗi kết nối máy chủ quản lý!"))
            }
        }
    }

    // ── Đăng ký ───────────────────────────────────────────────────────────────
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid    = result.user?.uid ?: throw Exception("UID not found")

            val userDoc = hashMapOf(
                "uid"       to uid,
                "fullName"  to fullName,
                "email"     to email,
                "phone"     to phone,
                "role"      to "customer",
                "createdAt" to Timestamp.now()
            )
            db.collection("users").document(uid).set(userDoc).await()
            syncWithPHP(result.user!!)
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Lấy thông tin profile từ Firestore ────────────────────────────────────
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")
            val doc = db.collection("users").document(uid).get().await()
            val profile = UserProfile(
                uid      = uid,
                fullName = doc.getString("fullName") ?: "Người dùng",
                email    = doc.getString("email")    ?: auth.currentUser?.email ?: "",
                phone    = doc.getString("phone")    ?: "",
                role     = doc.getString("role")     ?: "customer"
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()
    fun getCurrenUser(): FirebaseUser? = auth.currentUser

    // ── Đăng nhập / đăng ký bằng Google ─────────────────────────────────────
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider
                .getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user   = result.user ?: throw Exception("User not found")

            // Nếu là lần đầu (tài khoản mới) → tạo document Firestore
            val isNewUser = result.additionalUserInfo?.isNewUser == true
            if (isNewUser) {
                val userDoc = hashMapOf(
                    "uid"       to user.uid,
                    "fullName"  to (user.displayName ?: ""),
                    "email"     to (user.email ?: ""),
                    "phone"     to (user.phoneNumber ?: ""),
                    "role"      to "customer",
                    "provider"  to "google",
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("users").document(user.uid).set(userDoc).await()
            }
            syncWithPHP(user)

            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Gửi email đặt lại mật khẩu ─────────────────────────────────────────────
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Đăng nhập bằng Facebook ────────────────────────────────────────
    // accessToken: lấy từ Facebook SDK sau khi user chấp nhận
    suspend fun signInWithFacebook(accessToken: String): Result<String> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = auth.signInWithCredential(credential).await()
            val user   = result.user ?: throw Exception("User not found")

            // Nếu lần đầu → tạo Firestore document
            val isNewUser = result.additionalUserInfo?.isNewUser == true
            if (isNewUser) {
                val userDoc = hashMapOf(
                    "uid"       to user.uid,
                    "fullName"  to (user.displayName ?: ""),
                    "email"     to (user.email ?: ""),
                    "phone"     to (user.phoneNumber ?: ""),
                    "role"      to "customer",
                    "provider"  to "facebook",
                    "createdAt" to Timestamp.now()
                )
                db.collection("users").document(user.uid).set(userDoc).await()
            }
            syncWithPHP(user)

            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ── Gửi đồng bộ lên PHP (Chạy ngầm) ──────────────────────────────────────
    private fun syncWithPHP(user: FirebaseUser) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                // Lấy thông tin từ Firestore vì FirebaseUser có thể bị null tên và SĐT
                val doc = db.collection("users").document(user.uid).get().await()
                val firestoreName = doc.getString("fullName") ?: user.displayName ?: "Khách hàng"
                val firestorePhone = doc.getString("phone") ?: user.phoneNumber

                val syncRequest = com.example.da_cuoiky.model.SyncUserRequest(
                    uid = user.uid,
                    name = firestoreName,
                    email = user.email ?: "",
                    phone = firestorePhone
                )

                val apiService = com.example.da_cuoiky.network.RetrofitClient.instance
                val response = apiService.syncUserToMySQL(syncRequest)
                if (response.isSuccessful) {
                    android.util.Log.d("SYNC", "Đồng bộ user sang MySQL thành công!")
                } else {
                    android.util.Log.e("SYNC", "Lỗi đồng bộ MySQL: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("SYNC", "Ngoại lệ khi đồng bộ: ${e.message}")
            }
        }
    }
}
// ── Data class thông tin người dùng ───────────────────────────────────────────
data class UserProfile(
    val uid: String      = "",
    val fullName: String = "",
    val email: String    = "",
    val phone: String    = "",
    val role: String     = "customer"
)