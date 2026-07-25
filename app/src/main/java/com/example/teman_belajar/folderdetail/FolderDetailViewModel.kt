package com.example.teman_belajar.folderdetail

import android.app.Application
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.MaterialUploadRequest
import com.example.teman_belajar.fetch.model.MaterialUploadSuccessRequest
import com.example.teman_belajar.fetch.model.RenameFolderRequest
import com.example.teman_belajar.fetch.model.RenameMaterialRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import java.util.UUID

enum class FileType(val mimeTypes: List<String>) {
    IMAGE(listOf("image/jpeg", "image/jpg", "image/png", "image/webp", "image/heic", "image/heif")),
    DOCUMENT(listOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain"
    )),
    PPT(listOf(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    ));

    companion object {
        fun fromMimeType(mimeType: String?): FileType? {
            if (mimeType.isNullOrBlank()) return null
            val normalized = mimeType.lowercase().trim()
            if (normalized.startsWith("image/")) return IMAGE
            return entries.find { it.mimeTypes.any { m -> m.lowercase() == normalized } }
        }
    }
}

data class DummyFile(
    val id: String,
    val name: String,
    val mimeType: String = "",
    val uri: String? = null,
    val isSmartSummary: Boolean = false,
    val size: String = "0 MB",
    val description: String = ""
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
    val materials: List<DummyFile> = emptyList(),
    val smartSummaries: List<DummyFile> = emptyList(),

    val isFileOptionsVisible: Boolean = false,
    val isRenameFileDialogVisible: Boolean = false,
    val isDeleteFileDialogVisible: Boolean = false,
    val selectedFile: DummyFile? = null,

    val isFolderOptionsVisible: Boolean = false,
    val isRenameFolderDialogVisible: Boolean = false,
    val isDeleteFolderDialogVisible: Boolean = false,
    val newFolderName: String = "",

    val isGenerateQuizSelected: Boolean = false,
    val isSummarySelectionMode: Boolean = false,
    val selectedMaterialIds: Set<String> = emptySet(),

    val pendingDeleteIds: Set<String> = emptySet()
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

    object ShowFolderOptions : FolderDetailEvent()
    object DismissFolderOptions : FolderDetailEvent()
    object RenameFolderClicked : FolderDetailEvent()
    object DeleteFolderClicked : FolderDetailEvent()
    data class NewFolderNameChanged(val name: String) : FolderDetailEvent()
    object DismissRenameFolderDialog : FolderDetailEvent()
    object DismissDeleteFolderDialog : FolderDetailEvent()
    object ConfirmRenameFolder : FolderDetailEvent()
    object ConfirmDeleteFolder : FolderDetailEvent()

    data class ToggleMaterialSelection(val fileId: String) : FolderDetailEvent()
    object ConfirmSmartSummary : FolderDetailEvent()
    object CancelSummarySelection : FolderDetailEvent()

    object ClearError : FolderDetailEvent()
    object ClearSuccessMessage : FolderDetailEvent()
    data class FileClicked(val file: DummyFile) : FolderDetailEvent()
    data class DownloadFileClicked(val file: DummyFile) : FolderDetailEvent()
}

class FolderDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(FolderDetailUiState())
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    var onNavigateBack: (() -> Unit)? = null
    var onOpenFile: ((String, String) -> Unit)? = null
    var onDownloadFile: ((String, String) -> Unit)? = null
    var onNavigateToSummaryDetail: (() -> Unit)? = null

    fun setFolderData(id: String, name: String) {
        if (id.isEmpty()) return

        _uiState.value = FolderDetailUiState(
            folderId = id,
            folderName = name,
            isLoading = true
        )

        loadMaterials(id)
    }

    private suspend fun fetchMaterialsInternal(folderId: String): List<DummyFile> = coroutineScope {
        val response = apiService.getFolderMaterials(folderId)
        if (response.isSuccessful) {
            val materials = response.body() ?: emptyList()
            val currentFilesMap = _uiState.value.allFiles.associateBy { it.id }

            materials.map { material ->
                async {
                    val existing = currentFilesMap[material.fileId]
                    if (existing?.uri != null && existing.name == material.fileName) {
                        existing
                    } else {
                        val infoResponse = try {
                            apiService.getMaterialInfo(material.fileId, material.fileName)
                        } catch (e: Exception) { null }
                        val url = if (infoResponse?.isSuccessful == true) infoResponse.body()?.url else null
                        
                        val isAI = material.fileType == "SUMMARY" || material.fileName.contains("(AI)", ignoreCase = true)
                        
                        DummyFile(
                            id = material.fileId,
                            name = material.fileName,
                            mimeType = material.fileType,
                            uri = url,
                            isSmartSummary = isAI,
                            description = if (isAI) "Ringkasan poin-poin utama. (AI)" else "",
                            size = if (!isAI) "5 MB" else "0 MB"
                        )
                    }
                }
            }.awaitAll().filter { it.id !in _uiState.value.pendingDeleteIds }
        } else {
            emptyList()
        }
    }

    private fun loadMaterials(folderId: String) {
        if (folderId.isEmpty()) return

        loadingJob?.cancel()

        if (!_uiState.value.isLoading && _uiState.value.allFiles.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }

        loadingJob = viewModelScope.launch {
            try {
                val files = fetchMaterialsInternal(folderId)
                _uiState.update { state ->
                    val filtered = files.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
                    state.copy(
                        allFiles = files,
                        materials = filtered.filter { !it.isSmartSummary },
                        smartSummaries = filtered.filter { it.isSmartSummary },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Jaringan bermasalah") }
            }
        }
    }

    private fun uploadMaterial(name: String, mimeType: String, uriString: String?) {
        val folderId = _uiState.value.folderId
        if (folderId.isEmpty() || uriString == null) return

        _uiState.update { it.copy(isLoading = true, isAddFileMenuVisible = false) }

        viewModelScope.launch {
            try {
                val request = MaterialUploadRequest(folderId, name, mimeType)
                val response = apiService.uploadMaterial(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val signedUrl = body?.url ?: ""
                    val materialId = body?.fileName ?: ""

                    if (signedUrl.isNotEmpty()) {
                        val contentUri = uriString.toUri()
                        val inputStream = getApplication<Application>().contentResolver.openInputStream(contentUri)
                        val fileBytes = inputStream?.use { it.readBytes() }

                        if (fileBytes != null) {
                            val putCode = withContext(Dispatchers.IO) {
                                val uploadRequest = Request.Builder()
                                    .url(signedUrl).put(object : RequestBody() {
                                        override fun contentType() = mimeType.toMediaTypeOrNull()
                                        override fun contentLength() = fileBytes.size.toLong()
                                        override fun writeTo(sink: BufferedSink) { sink.write(fileBytes) }
                                    }).build()
                                OkHttpClient().newCall(uploadRequest).execute().use { it.code }
                            }

                            if (putCode in 200..299) {
                                if (apiService.notifyUploadSuccess(MaterialUploadSuccessRequest(materialId, signedUrl.substringBefore("?"))).isSuccessful) {
                                    loadMaterials(folderId)
                                    _uiState.update { it.copy(
                                        searchQuery = "",
                                        successMessage = "Berhasil diunggah!",
                                        isLoading = false
                                    ) }
                                    return@launch
                                }
                            }
                        }
                    }
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal upload") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun deleteMaterial(file: DummyFile) {
        val oldState = _uiState.value

        _uiState.update { state ->
            val updated = state.allFiles.filter { it.id != file.id }
            val filtered = updated.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
            state.copy(
                allFiles = updated,
                materials = filtered.filter { !it.isSmartSummary },
                smartSummaries = filtered.filter { it.isSmartSummary },
                isDeleteFileDialogVisible = false,
                selectedFile = null,
                isFileOptionsVisible = false,
                pendingDeleteIds = state.pendingDeleteIds + file.id
            )
        }

        viewModelScope.launch {
            try {
                if (apiService.deleteMaterial(file.id).isSuccessful) {
                    _uiState.update { it.copy(successMessage = "Terhapus!") }
                } else {
                    _uiState.update { it.copy(allFiles = oldState.allFiles, materials = oldState.materials, smartSummaries = oldState.smartSummaries) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(allFiles = oldState.allFiles, materials = oldState.materials, smartSummaries = oldState.smartSummaries) }
            }
        }
    }

    private fun renameMaterial() {
        val file = _uiState.value.selectedFile ?: return
        val newName = _uiState.value.newFileName.trim()
        if (newName.isEmpty()) return

        val oldState = _uiState.value
        _uiState.update { state ->
            val updated = state.allFiles.map { if (it.id == file.id) it.copy(name = newName) else it }
            val filtered = updated.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
            state.copy(
                allFiles = updated,
                materials = filtered.filter { !it.isSmartSummary },
                smartSummaries = filtered.filter { it.isSmartSummary },
                isRenameFileDialogVisible = false,
                selectedFile = null
            )
        }

        viewModelScope.launch {
            try {
                if (apiService.renameMaterial(RenameMaterialRequest(file.id, newName)).isSuccessful) {
                    _uiState.update { it.copy(successMessage = "Nama diubah!") }
                } else {
                    _uiState.update { it.copy(allFiles = oldState.allFiles, materials = oldState.materials, smartSummaries = oldState.smartSummaries) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(allFiles = oldState.allFiles, materials = oldState.materials, smartSummaries = oldState.smartSummaries) }
            }
        }
    }

    private fun renameFolder() {
        val folderId = _uiState.value.folderId
        val newName = _uiState.value.newFolderName.trim()
        if (folderId.isEmpty() || newName.isEmpty()) return

        viewModelScope.launch {
            try {
                val response = apiService.renameFolder(RenameFolderRequest(UUID.fromString(folderId), newName))
                if (response.isSuccessful) {
                    _uiState.update { it.copy(folderName = newName, isRenameFolderDialogVisible = false, successMessage = "Folder diubah!") }
                } else {
                    _uiState.update { it.copy(errorMessage = "Gagal mengubah folder") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun deleteFolder() {
        val folderId = _uiState.value.folderId
        if (folderId.isEmpty()) return

        viewModelScope.launch {
            try {
                val response = apiService.deleteFolder(UUID.fromString(folderId))
                if (response.isSuccessful) {
                    onNavigateBack?.invoke()
                } else {
                    _uiState.update { it.copy(errorMessage = "Gagal menghapus folder") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onEvent(event: FolderDetailEvent) {
        when (event) {
            FolderDetailEvent.NavigateBack -> {
                onNavigateBack?.invoke()
            }
            FolderDetailEvent.Refresh -> loadMaterials(_uiState.value.folderId)
            is FolderDetailEvent.SearchQueryChanged -> {
                _uiState.update { state ->
                    val filtered = state.allFiles.filter { it.name.contains(event.query, ignoreCase = true) }
                    state.copy(
                        searchQuery = event.query,
                        materials = filtered.filter { !it.isSmartSummary },
                        smartSummaries = filtered.filter { it.isSmartSummary }
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
            FolderDetailEvent.DismissRenameFileDialog -> _uiState.update { it.copy(isRenameFileDialogVisible = false, selectedFile = null) }
            FolderDetailEvent.DismissDeleteFileDialog -> _uiState.update { it.copy(isDeleteFileDialogVisible = false, selectedFile = null) }
            FolderDetailEvent.ConfirmRenameFile -> renameMaterial()
            FolderDetailEvent.ConfirmDeleteFile -> { _uiState.value.selectedFile?.let { deleteMaterial(it) } }

            FolderDetailEvent.ShowFolderOptions -> _uiState.update { it.copy(isFolderOptionsVisible = true) }
            FolderDetailEvent.DismissFolderOptions -> _uiState.update { it.copy(isFolderOptionsVisible = false) }
            FolderDetailEvent.RenameFolderClicked -> {
                val folderName = _uiState.value.folderName
                _uiState.update { it.copy(isFolderOptionsVisible = false, isRenameFolderDialogVisible = true, newFolderName = folderName) }
            }
            FolderDetailEvent.DeleteFolderClicked -> _uiState.update { it.copy(isFolderOptionsVisible = false, isDeleteFolderDialogVisible = true) }
            is FolderDetailEvent.NewFolderNameChanged -> _uiState.update { it.copy(newFolderName = event.name) }
            FolderDetailEvent.DismissRenameFolderDialog -> _uiState.update { it.copy(isRenameFolderDialogVisible = false) }
            FolderDetailEvent.DismissDeleteFolderDialog -> _uiState.update { it.copy(isDeleteFolderDialogVisible = false) }
            FolderDetailEvent.ConfirmRenameFolder -> renameFolder()
            FolderDetailEvent.ConfirmDeleteFolder -> deleteFolder()

            FolderDetailEvent.GenerateQuizClicked -> {
                viewModelScope.launch {
                    try {
                        val res = apiService.generateQuiz(_uiState.value.folderId)
                        if (res.isSuccessful) _uiState.update { it.copy(successMessage = "Kuis sedang dibuat!") }
                    } catch (e: Exception) {}
                }
            }
            FolderDetailEvent.SmartSummaryClicked -> {
                _uiState.update { it.copy(isSummarySelectionMode = true, selectedMaterialIds = emptySet()) }
            }
            is FolderDetailEvent.ToggleMaterialSelection -> {
                _uiState.update { state ->
                    val next = if (state.selectedMaterialIds.contains(event.fileId)) {
                        state.selectedMaterialIds - event.fileId
                    } else {
                        state.selectedMaterialIds + event.fileId
                    }
                    state.copy(selectedMaterialIds = next)
                }
            }
            FolderDetailEvent.CancelSummarySelection -> {
                _uiState.update { it.copy(isSummarySelectionMode = false, selectedMaterialIds = emptySet()) }
            }
            FolderDetailEvent.ConfirmSmartSummary -> {
                val selectedIds = _uiState.value.selectedMaterialIds
                if (selectedIds.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = "Pilih minimal satu materi") }
                    return
                }
                
                onNavigateToSummaryDetail?.invoke()

                _uiState.update { it.copy(
                    isSummarySelectionMode = false,
                    selectedMaterialIds = emptySet(),
                    isLoading = false
                ) }

                viewModelScope.launch {
                    try {
                        apiService.smartSummary(_uiState.value.folderId)
                    } catch (e: Exception) {
                    }
                }
            }

            FolderDetailEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            FolderDetailEvent.ClearSuccessMessage -> _uiState.update { it.copy(successMessage = null) }

            is FolderDetailEvent.FileClicked -> {
                val file = event.file
                if (_uiState.value.isSummarySelectionMode && !file.isSmartSummary) {
                    onEvent(FolderDetailEvent.ToggleMaterialSelection(file.id))
                    return
                }
                if (file.isSmartSummary) {
                    onNavigateToSummaryDetail?.invoke()
                    return
                }
                if (file.uri != null) onOpenFile?.invoke(file.uri, file.mimeType)
                else {
                    viewModelScope.launch {
                        _uiState.update { it.copy(selectedFile = file, isLoading = true) }
                        try {
                            val res = apiService.getMaterialInfo(file.id, file.name)
                            if (res.isSuccessful && res.body()?.url != null) onOpenFile?.invoke(res.body()!!.url, file.mimeType)
                        } finally {
                            _uiState.update { it.copy(isLoading = false, selectedFile = null) }
                        }
                    }
                }
            }
            is FolderDetailEvent.DownloadFileClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    try {
                        val response = apiService.downloadMaterial(event.file.id)
                        if (response.isSuccessful) {
                            val downloadUrl = response.body()?.url
                            if (downloadUrl != null) {
                                onDownloadFile?.invoke(downloadUrl, event.file.name)
                                _uiState.update { it.copy(successMessage = "Memulai download...") }
                            } else {
                                _uiState.update { it.copy(errorMessage = "Link download tidak valid") }
                            }
                        } else {
                            _uiState.update { it.copy(errorMessage = "Gagal mendapatkan link download") }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Terjadi kesalahan: ${e.message}") }
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
