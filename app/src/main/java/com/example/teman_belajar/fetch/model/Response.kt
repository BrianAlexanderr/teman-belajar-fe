package com.example.teman_belajar.fetch.model

import android.R
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class LoginResponse(
    val userName: String?,
    val token: String?,
    val refreshToken: String?
)

data class GeneralResponse(
    val message: String?,
    val timeStamp: String?
)

data class VerifyOTPResponse(
    val token : String?
)

data class UserFolderResponse(
    val id: UUID?,
    val name: String?
)

data class CreateFolderResponse(
    val msg: String?,
    val createdAt: String?
)

data class MaterialResponse(
    val fileName: String?,
    val url: String?
)

data class FolderMaterialResponse(
    val fileId: String?,
    val fileName: String?,
    val fileType: String?
)

data class SummaryListItemResponse(
    val id: String?,
    val title: String?,
    val preview: String?
)

data class SummaryDetailResponse(
    val id: String?,
    val title: String?,
    val keyPoint: List<String>?,
    val content: String?
)

data class QuizItemResponse(
    val id: String?,
    val question: String?,
    val answerA: String?,
    val answerB: String?,
    val answerC: String?,
    val answerD: String?,
    val answerCorrect: String?
)

data class SaveAndSubmitQuizResponse(
    val message: String?,
    val score: String,
    val timeStamp: String?
)

data class QuizListItemResponse(
    val id: String?,
    val title: String?,
    val createdAt: String?
)

data class CheckQuizResponse(
    val correctAnswer: String?,
    val isCorrect: Boolean?,
    val correctAnswerField: String?,
    val explanation: String?
)

data class QuizAttemptedResponse(
    val quizId: String?,
    val attemptedQuizId: String?,
    val quizTitle: String?,
    val folderName: String?,
    val score: String?,
    val countAttemptedQuestion: String?,
    val attemptedAt: String?
)

data class QuizAttemptedDetailResponse(
    val quizId: String?,
    val questionAttemptResponse: List<QuestionAttemptDetailResponse>?
)

data class QuestionAttemptDetailResponse(
    val questionId: String?,
    val isCorrect: Boolean?,
    val selectedAnswer: String?,
    @SerializedName("explaination")
    val explanation: String?
)
