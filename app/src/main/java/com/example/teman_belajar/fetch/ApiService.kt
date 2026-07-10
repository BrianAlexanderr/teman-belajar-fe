package com.example.teman_belajar.fetch

import android.content.Context
import android.os.Build
import com.example.teman_belajar.BuildConfig
import com.example.teman_belajar.fetch.model.ChangePasswordRequest
import com.example.teman_belajar.fetch.model.CreateFolderRequest
import com.example.teman_belajar.fetch.model.CreateFolderResponse
import com.example.teman_belajar.fetch.model.ForgotPasswordRequest
import com.example.teman_belajar.fetch.model.GeneralResponse
import com.example.teman_belajar.fetch.model.LoginRequest
import com.example.teman_belajar.fetch.model.LoginResponse
import com.example.teman_belajar.fetch.model.RefreshTokenRequst
import com.example.teman_belajar.fetch.model.RegisterRequest
import com.example.teman_belajar.fetch.model.RenameFolderRequest
import com.example.teman_belajar.fetch.model.UserFolderResponse
import com.example.teman_belajar.fetch.model.VerifyOTPRequest
import com.example.teman_belajar.fetch.model.VerifyOTPResponse
import com.example.teman_belajar.utils.datastore.UserPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID


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

class AuthInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val token = TokenManager.accessToken

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

interface TokenRefreshApi {
    @POST("/api/auth/refresh-token")
    fun refreshToken(@Body request: RefreshTokenRequst): retrofit2.Call<LoginResponse>
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

    @POST("/api/auth/verify-otp")
    suspend fun verifyOTP(@Body request: VerifyOTPRequest) : Response<VerifyOTPResponse>

    @GET("/api/folders/user")
    suspend fun getUserFolder() : Response<List<UserFolderResponse>>

    @POST("/api/folders/create")
    suspend fun createFolder(@Body request: CreateFolderRequest) : Response<CreateFolderResponse>

    @PUT("/api/folders/update")
    suspend fun renameFolder(@Body request: RenameFolderRequest) : Response<Unit>

    @DELETE("/api/folders/{id}")
    suspend fun deleteFolder(@Path("id") id: UUID) : Response<Unit>

    companion object {
        val BASE_URL: String
            get() {
                return if (isEmulator()) {
                    BuildConfig.EMULATOR_IP
                } else {
                    BuildConfig.PHYSICAL_DEVICE_IP
                }
            }

        fun create(context: Context): ApiService {
            val userPreferences = UserPreferences(context)

            val authInterceptor = AuthInterceptor()
            val tokenAuthenticator = TokenAuthenticator(userPreferences)

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(tokenAuthenticator)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
