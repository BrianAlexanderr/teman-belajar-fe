package com.example.teman_belajar.fetch

import android.content.Context
import com.example.teman_belajar.fetch.model.RefreshTokenRequst
import com.example.teman_belajar.utils.SessionManager
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val userPreferences: UserPreferences
) : Authenticator {

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        val refreshToken = runBlocking {
            userPreferences.refreshTokenFlow.first()
        }
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
                    runBlocking {
                        userPreferences.setLoggedIn(true, userName, newRefreshToken ?: refreshToken)
                    }

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        runBlocking {
            userPreferences.setLoggedIn(false)
            SessionManager.triggerSessionExpired()
        }
        return null
    }
}