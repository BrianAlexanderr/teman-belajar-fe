package com.example.teman_belajar.fetch.model

import java.util.UUID

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ChangePasswordRequest(
    val email : String,
    val newPassword : String,
    val resetToken : String
)

data class VerifyOTPRequest(
    val email : String,
    val otp : String
)

data class RefreshTokenRequst(
    val token: String
)

data class CreateFolderRequest(
    val name: String
)

data class RenameFolderRequest(
    val id: UUID,
    val newName: String
)


data class MaterialUploadRequest(
    val folderId: String,
    val fileName: String,
    val fileType: String
)

data class MaterialUploadSuccessRequest(
    val materialId: String,
    val path: String
)

data class RenameMaterialRequest(
    val id: String,
    val newName: String
)

data class SmartSummaryRequest(
    val folderId: String,
    val materialIds: List<String>
)
