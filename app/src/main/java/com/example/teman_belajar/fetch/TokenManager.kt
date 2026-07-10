package com.example.teman_belajar.fetch

object TokenManager {
    var accessToken: String? = null
    var refreshToken: String? = null

    fun initializeTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }
}