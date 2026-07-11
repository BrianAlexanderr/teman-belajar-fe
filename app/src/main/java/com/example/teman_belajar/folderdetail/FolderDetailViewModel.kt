package com.example.teman_belajar.folderdetail

import android.app.Application
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.MaterialUploadRequest
import com.example.teman_belajar.fetch.model.MaterialUploadSuccessRequest
import com.example.teman_belajar.fetch.model.RenameMaterialRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONObject

enum class FileType(val mimeTypes: List<String>) {
    IMAGE(listOf("image/jpeg", "image/png", "image/webp")),
    DOCUMENT(listOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )),
    PPT(listOf(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    ));

    companion object {
        fun fromMimeType(mimeType: String): FileType? {
            return entries.find { it.mimeTypes.contains(mimeType) }
        }
    }
}

data class DummyFile(
    val id: String,
    val name: String,
    val mimeType: String = "",
    val uri: String? = null
)

data class FolderDetailUiState(
    val folderId: String = "",
    val folderName: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val isAddFileMenuVisible: Boolean = false,
    val fileCounter: Int = 1,
    val newFileName: String = "",
    val allFiles: List<DummyFile> = emptyList(),
    val files: List<DummyFile> = emptyList(),

    val isFileOptionsVisible: Boolean = false,
    val isRenameFileDialogVisible: Boolean = false,
    val isDeleteFileDialogVisible: Boolean = false,
    val selectedFile: DummyFile? = null,

    val isGenerateQuizSelected: Boolean = false,
    val isSmartSummarySelected: Boolean = false
)

sealed class FolderDetailEvent {
    object NavigateBack : FolderDetailEvent()
    object Refresh : FolderDetailEvent()
    data class SearchQueryChanged(val query: String) : FolderDetailEvent()
    object GenerateQuizClicked : FolderDetailEvent()
    object SmartSummaryClicked : FolderDetailEvent()

    object AddMateriClicked : FolderDetailEvent()
    object DismissAddFileMenu : FolderDetailEvent()
    data class FileAdded(val name: String, val mimeType: String, val uri: String?) : FolderDetailEvent()
    data class NewFileNameChanged(val name: String) : FolderDetailEvent()

    data class ShowFileOptions(val file: DummyFile) : FolderDetailEvent()
    object DismissFileOptions : FolderDetailEvent()
    object RenameFileClicked : FolderDetailEvent()
    object DeleteFileClicked : FolderDetailEvent()
    object DismissRenameFileDialog : FolderDetailEvent()
    object DismissDeleteFileDialog : FolderDetailEvent()
    object ConfirmRenameFile : FolderDetailEvent()
    object ConfirmDeleteFile : FolderDetailEvent()
    object ClearError : FolderDetailEvent()
    object ClearSuccessMessage : FolderDetailEvent()
    data class FileClicked(val file: DummyFile) : FolderDetailEvent()
}

class FolderDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(FolderDetailUiState())
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onOpenFile: ((String, String) -> Unit)? = null

    fun setFolderData(id: String, name: String) {
        _uiState.update { it.copy(folderId = id, folderName = name) }
        loadMaterials(id)
    }

    private fun loadMaterials(folderId: String) {
        if (folderId.isEmpty()) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getFolderMaterials(folderId)
                if (response.isSuccessful) {
                    val materials = response.body() ?: emptyList()
                    val files = materials.map { material ->
                        DummyFile(
                            id = material.fileId,
                            name = material.fileName,
                            mimeType = material.fileType,
                            uri = null
                        )
                    }
                    _uiState.update { state ->
                        state.copy(
                            allFiles = files,
                            files = files.filter { it.name.contains(state.searchQuery, ignoreCase = true) },
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = parseError(response)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
            }
        }
    }

    private fun uploadMaterial(name: String, mimeType: String, uriString: String?) {
        val folderId = _uiState.value.folderId
        if (folderId.isEmpty() || uriString == null) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isAddFileMenuVisible = false) }

        viewModelScope.launch {
            try {
                val request = MaterialUploadRequest(folderId = folderId, fileName = name, fileType = mimeType)
                val response = apiService.uploadMaterial(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val materialId = body?.fileName ?: "" 
                    val signedUrl = body?.url ?: ""

                    if (signedUrl.isNotEmpty()) {
                        val context = getApplication<Application>()
                        val contentUri = uriString.toUri()
                        val inputStream = context.contentResolver.openInputStream(contentUri)
                        val fileBytes = inputStream?.use { it.readBytes() }

                        if (fileBytes != null) {
                            val mediaType = mimeType.toMediaTypeOrNull()

                            val requestBody = object : RequestBody() {
                                override fun contentType() = mediaType
                                override fun contentLength() = fileBytes.size.toLong()
                                override fun writeTo(sink: BufferedSink) {
                                    sink.write(fileBytes)
                                }
                            }

                            val putResponseCode = withContext(Dispatchers.IO) {
                                val uploadClient = OkHttpClient.Builder().build()
                                val uploadRequest = Request.Builder()
                                    .url(signedUrl)
                                    .put(requestBody)
                                    .header("Content-Type", mimeType)
                                    .build()

                                uploadClient.newCall(uploadRequest).execute().use { putResponse ->
                                    putResponse.code
                                }
                            }

                            if (putResponseCode in 200..299) {
                                val cleanPath = signedUrl.substringBefore("?")
                                val notifyResponse = apiService.notifyUploadSuccess(
                                    MaterialUploadSuccessRequest(materialId = materialId, path = cleanPath)
                                )

                                if (notifyResponse.isSuccessful) {
                                    _uiState.update { it.copy(searchQuery = "") } // Reset search agar file baru terlihat
                                    loadMaterials(folderId)
                                    _uiState.update { it.copy(successMessage = "Berhasil diunggah!", isLoading = false) }
                                } else {
                                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal sinkronisasi data") }
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal unggah ke storage (Status: $putResponseCode)") }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal membaca file") }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "URL tidak valid") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = parseError(response)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun deleteMaterial(file: DummyFile) {
        _uiState.update { it.copy(isLoading = true, isDeleteFileDialogVisible = false, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = apiService.deleteMaterial(file.id)
                if (response.isSuccessful) {
                    loadMaterials(_uiState.value.folderId)
                    _uiState.update { it.copy(successMessage = "Materi dihapus!") }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = parseError(response)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error jaringan") }
            }
        }
    }

    private fun renameMaterial() {
        val file = _uiState.value.selectedFile ?: return
        val newName = _uiState.value.newFileName.trim()
        if (newName.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, isRenameFileDialogVisible = false, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = apiService.renameMaterial(RenameMaterialRequest(id = file.id, newName = newName))
                if (response.isSuccessful) {
                    loadMaterials(_uiState.value.folderId)
                    _uiState.update { it.copy(successMessage = "Nama materi berhasil diubah!", isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = parseError(response)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
            }
        }
    }

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) JSONObject(errorBody).optString("message", "Gagal")
            else "Gagal"
        } catch (e: Exception) { "Gagal" }
    }

    fun onEvent(event: FolderDetailEvent) {
        when (event) {
            FolderDetailEvent.NavigateBack -> onNavigateBack?.invoke()
            FolderDetailEvent.Refresh -> loadMaterials(_uiState.value.folderId)
            is FolderDetailEvent.SearchQueryChanged -> {
                _uiState.update { state ->
                    state.copy(
                        searchQuery = event.query,
                        files = state.allFiles.filter { it.name.contains(event.query, ignoreCase = true) }
                    )
                }
            }
            FolderDetailEvent.AddMateriClicked -> _uiState.update { it.copy(isAddFileMenuVisible = true) }
            FolderDetailEvent.DismissAddFileMenu -> _uiState.update { it.copy(isAddFileMenuVisible = false) }
            is FolderDetailEvent.FileAdded -> uploadMaterial(event.name, event.mimeType, event.uri)
            is FolderDetailEvent.NewFileNameChanged -> _uiState.update { it.copy(newFileName = event.name) }
            is FolderDetailEvent.ShowFileOptions -> _uiState.update { it.copy(isFileOptionsVisible = true, selectedFile = event.file) }
            FolderDetailEvent.DismissFileOptions -> _uiState.update { it.copy(isFileOptionsVisible = false, selectedFile = null) }
            FolderDetailEvent.RenameFileClicked -> {
                val file = _uiState.value.selectedFile
                _uiState.update { it.copy(isFileOptionsVisible = false, isRenameFileDialogVisible = true, newFileName = file?.name ?: "") }
            }
            FolderDetailEvent.DeleteFileClicked -> _uiState.update { it.copy(isFileOptionsVisible = false, isDeleteFileDialogVisible = true) }
            FolderDetailEvent.DismissRenameFileDialog -> _uiState.update { it.copy(isRenameFileDialogVisible = false) }
            FolderDetailEvent.DismissDeleteFileDialog -> _uiState.update { it.copy(isDeleteFileDialogVisible = false) }
            FolderDetailEvent.ConfirmRenameFile -> renameMaterial()
            FolderDetailEvent.ConfirmDeleteFile -> _uiState.value.selectedFile?.let { deleteMaterial(it) }
            FolderDetailEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            FolderDetailEvent.ClearSuccessMessage -> _uiState.update { it.copy(successMessage = null) }
            FolderDetailEvent.GenerateQuizClicked -> {
                _uiState.update { state ->
                    state.copy(
                        isGenerateQuizSelected = !state.isGenerateQuizSelected,
                        isSmartSummarySelected = false
                    )
                }
            }
            FolderDetailEvent.SmartSummaryClicked -> {
                _uiState.update { state ->
                    state.copy(
                        isSmartSummarySelected = !state.isSmartSummarySelected,
                        isGenerateQuizSelected = false
                    )
                }
            }
            is FolderDetailEvent.FileClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, selectedFile = event.file, errorMessage = null) }
                    try {
                        val response = apiService.getMaterialInfo(event.file.id, event.file.name)
                        if (response.isSuccessful) {
                            val url = response.body()?.url
                            if (!url.isNullOrEmpty()) {
                                onOpenFile?.invoke(url, event.file.mimeType)
                            } else {
                                _uiState.update { it.copy(errorMessage = "URL file tidak ditemukan") }
                            }
                        } else {
                            _uiState.update { it.copy(errorMessage = parseError(response)) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Gagal memuat file") }
                    } finally {
                        _uiState.update { it.copy(isLoading = false, selectedFile = null) }
                    }
                }
            }
        }
    }
}
