package com.example.da_cuoiky.fiebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepoesitory {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Đăng nhập ─────────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: throw Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
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

            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
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