package com.example.teman_belajar.fetch

import com.example.teman_belajar.fetch.model.RefreshTokenRequst
import com.example.teman_belajar.utils.SessionManager
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val userPreferences: UserPreferences
) : Authenticator {

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        val refreshToken = TokenManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            return null
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val refreshApi = retrofit.create(TokenRefreshApi::class.java)

        try {
            val refreshResponse = refreshApi.refreshToken(RefreshTokenRequst(refreshToken)).execute()

            if (refreshResponse.isSuccessful) {
                val newAccessToken = refreshResponse.body()?.token
                val newRefreshToken = refreshResponse.body()?.refreshToken
                val userName = refreshResponse.body()?.userName

                if (newAccessToken != null) {
                    TokenManager.accessToken = newAccessToken
                    TokenManager.refreshToken = newRefreshToken ?: refreshToken

                    CoroutineScope(Dispatchers.IO).launch {
                        userPreferences.setLoggedIn(true, userName, TokenManager.refreshToken)
                    }

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        TokenManager.accessToken = null
        TokenManager.refreshToken = null

        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.setLoggedIn(false)
            SessionManager.triggerSessionExpired()
        }
        return null
    }
}