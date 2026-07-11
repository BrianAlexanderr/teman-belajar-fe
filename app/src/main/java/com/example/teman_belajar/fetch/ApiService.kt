package com.example.teman_belajar.fetch

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.teman_belajar.BuildConfig
import com.example.teman_belajar.fetch.model.*
import com.example.teman_belajar.utils.datastore.UserPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
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

class SelectiveLoggingInterceptor : Interceptor {
    private val logger = HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        Log.d(
            "AUTH",
            "URL=${request.url}\nAuthorization=${request.header("Authorization")}"
        )
        val url = request.url.toString()
        val shouldLog = url.contains("/api/materials/upload") || request.method == "PUT"

        return if (shouldLog) {
            logger.intercept(chain)
        } else {
            chain.proceed(request)
        }
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

    @GET("/api/folders/{id}")
    suspend fun getFolderById(@Path("id") id: UUID) : Response<UserFolderResponse>

    @POST("/api/ai/generate-quiz/{folderId}")
    suspend fun generateQuiz(@Path("folderId") folderId: String) : Response<GeneralResponse>

    @POST("/api/ai/smart-summary/{folderId}")
    suspend fun smartSummary(@Path("folderId") folderId: String) : Response<GeneralResponse>

    @GET("/api/materials/folder/{folderId}")
    suspend fun getFolderMaterials(@Path("folderId") folderId: String) : Response<List<FolderMaterialResponse>>

    @GET("/api/materials/info/{fileId}/{fileName}")
    suspend fun getMaterialInfo(@Path("fileId") fileId: String, @Path("fileName") fileName: String) : Response<MaterialResponse>

    @POST("/api/materials/upload")
    suspend fun uploadMaterial(@Body request: MaterialUploadRequest) : Response<MaterialResponse>

    @POST("/api/materials/upload/success")
    suspend fun notifyUploadSuccess(@Body request: MaterialUploadSuccessRequest) : Response<Unit>

    @DELETE("/api/materials/{id}")
    suspend fun deleteMaterial(@Path("id") id: String) : Response<Unit>

    @PUT("/api/materials/rename")
    suspend fun renameMaterial(@Body request: RenameMaterialRequest) : Response<Unit>

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
