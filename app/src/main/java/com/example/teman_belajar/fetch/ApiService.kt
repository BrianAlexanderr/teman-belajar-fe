package com.example.teman_belajar.fetch

import android.os.Build
import com.example.teman_belajar.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class GeneralResponse(
    val message: String,
    val timeStamp: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ChangePasswordRequest(
    val email : String,
    val newPassword : String,
    val otp : String
)

fun isEmulator(): Boolean {
    return (
            Build.FINGERPRINT.contains("generic") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("emulator")
            )
}

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<GeneralResponse>

    @POST("/api/auth/authenticate")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/send-otp")
    suspend fun forgotPass(@Body request: ForgotPasswordRequest) : Response<GeneralResponse>

    @POST("/api/auth/change-password")
    suspend fun changePass(@Body request: ChangePasswordRequest) : Response<GeneralResponse>

    companion object {
        private val BASE_URL: String
            get() {
                return if (isEmulator()) {
                    BuildConfig.EMULATOR_IP
                } else {
                    BuildConfig.PHYSICAL_DEVICE_IP
                }
            }

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
