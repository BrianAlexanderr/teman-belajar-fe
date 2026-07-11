package com.example.teman_belajar.fetch.model

import java.util.UUID

data class LoginResponse(
    val userName: String,
    val token: String,
    val refreshToken: String
)

data class GeneralResponse(
    val message: String,
    val timeStamp: String
)

data class VerifyOTPResponse(
    val token : String
)

data class UserFolderResponse(
    val id: UUID,
    val name: String
)

data class CreateFolderResponse(
    val msg: String,
    val createdAt: String
)

data class MaterialResponse(
    val fileName: String,
    val url: String
)

data class FolderMaterialResponse(
    val fileId: String,
    val fileName: String,
    val fileType: String
)
