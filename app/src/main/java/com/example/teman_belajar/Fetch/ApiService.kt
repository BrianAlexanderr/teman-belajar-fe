package com.example.teman_belajar.Fetch

import android.os.Build
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val timeStamp: String
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
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    companion object {
        private val BASE_URL: String
            get() {
                return if (isEmulator()) {
                    "http://10.0.2.2:8080/"
                } else {
                    //ini kalo pake device fisik pake ip sendiri ip config ygy
                    "http://10.248.163.193:8080/"
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
