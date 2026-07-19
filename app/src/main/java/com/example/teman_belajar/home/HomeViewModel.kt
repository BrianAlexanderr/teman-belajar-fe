package com.example.teman_belajar.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.CreateFolderRequest
import com.example.teman_belajar.fetch.model.RenameFolderRequest
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

data class FolderItem(val id: UUID, val name: String)

data class HomeUiState(
    val userName: String = "Pelajar",
    val allFolders: List<FolderItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val isPopupVisible: Boolean = false,
    val isCreateFolderDialogVisible: Boolean = false,
    val isRenameFolderDialogVisible: Boolean = false,
    val isDeleteFolderDialogVisible: Boolean = false,
    val isFolderOptionsVisible: Boolean = false,
    val selectedFolder: FolderItem? = null,
    val newFolderName: String = ""
)

sealed class HomeEvent {
    object FetchFolders : HomeEvent()
    object LogoutClicked : HomeEvent()
    data class SearchQueryChanged(val query: String) : HomeEvent()
    data class FolderClicked(val folder: FolderItem) : HomeEvent()
    object ConfirmCreateFolder : HomeEvent()
    object ConfirmRenameFolder : HomeEvent()
    object ConfirmDeleteFolder : HomeEvent()
    data class NewFolderNameChanged(val name: String) : HomeEvent()
    object QuizAiClicked : HomeEvent()
    object RingkasanClicked : HomeEvent()
    object ShowPopup : HomeEvent()
    object DismissPopup : HomeEvent()
    object ShowCreateFolderDialog : HomeEvent()
    object DismissCreateFolderDialog : HomeEvent()
    object DismissRenameFolderDialog : HomeEvent()
    object DismissDeleteFolderDialog : HomeEvent()
    data class ShowFolderOptions(val folder: FolderItem) : HomeEvent()
    object DismissFolderOptions : HomeEvent()
    object RenameFolderClicked : HomeEvent()
    object DeleteFolderClicked : HomeEvent()
    object ClearError : HomeEvent()
    object ClearSuccessMessage : HomeEvent()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var onNavigateToLogin: (() -> Unit)? = null
    var onNavigateToFolderDetail: ((String, String) -> Unit)? = null

    init {
        fetchUserName()
        fetchFolders()
    }

    private fun fetchUserName() {
        viewModelScope.launch {
            userPreferences.userNameFlow.collect { fullName ->
                if (!fullName.isNullOrBlank()) {
                    val firstName = fullName.trim().substringBefore(" ")
                    _uiState.update { it.copy(userName = firstName) }
                }
            }
        }
    }

    private fun fetchFolders(forceRefresh: Boolean = false) {
        if (_uiState.value.allFolders.isNotEmpty() && !forceRefresh) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getUserFolder()

                if (response.isSuccessful) {
                    val folderResponses = response.body() ?: emptyList()
                    val folderItems = folderResponses.map {
                        FolderItem(id = it.id, name = it.name)
                    }
                    _uiState.update { state ->
                        state.copy(
                            allFolders = folderItems,
                            folders = folderItems.filter {
                                it.name.contains(state.searchQuery, ignoreCase = true)
                            },
                            isLoading = false
                        )
                    }
                } else {
                    handleApiError(response, "Gagal memuat folder")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan")
                }
            }
        }
    }

    private fun createFolder(folderName: String) {
        _uiState.update { it.copy(isCreateFolderDialogVisible = false, isLoading = true, newFolderName = "") }
        viewModelScope.launch {
            try {
                val response = apiService.createFolder(CreateFolderRequest(name = folderName))
                if (response.isSuccessful) {
                    fetchFolders(true)
                    _uiState.update { it.copy(successMessage = "Folder berhasil dibuat!") }
                } else {
                    handleApiError(response, "Gagal membuat folder")
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
            }
        }
    }

    private fun renameFolder(id: UUID, newName: String) {
        _uiState.update { it.copy(isRenameFolderDialogVisible = false, isLoading = true, newFolderName = "", selectedFolder = null) }
        viewModelScope.launch {
            try {
                val response = apiService.renameFolder(RenameFolderRequest(id, newName))
                if (response.isSuccessful) {
                    fetchFolders(true)
                    _uiState.update { it.copy(successMessage = "Folder berhasil diubah!") }
                } else {
                    handleApiError(response, "Gagal mengubah nama folder")
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
            }
        }
    }

    private fun deleteFolder(id: UUID) {
        _uiState.update { it.copy(isDeleteFolderDialogVisible = false, isLoading = true, selectedFolder = null) }
        viewModelScope.launch {
            try {
                val response = apiService.deleteFolder(id)
                if (response.isSuccessful) {
                    fetchFolders(true)
                    _uiState.update { it.copy(successMessage = "Folder berhasil dihapus!") }
                } else {
                    handleApiError(response, "Gagal menghapus folder")
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
            }
        }
    }

    private fun handleApiError(response: retrofit2.Response<*>, defaultError: String) {
        if (response.code() == 401) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        val errorString = response.errorBody()?.string()
        val backendErrorMessage = try {
            if (!errorString.isNullOrEmpty()) {
                JSONObject(errorString).getString("msg")
            } else defaultError
        } catch (_: Exception) {
            defaultError
        }
        _uiState.update { it.copy(isLoading = false, errorMessage = backendErrorMessage) }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.FetchFolders -> fetchFolders()
            HomeEvent.LogoutClicked -> logout()
            is HomeEvent.SearchQueryChanged -> {
                _uiState.update { state ->
                    state.copy(
                        searchQuery = event.query,
                        folders = state.allFolders.filter {
                            it.name.contains(event.query, ignoreCase = true)
                        }
                    )
                }
            }

            is HomeEvent.FolderClicked -> {
                onNavigateToFolderDetail?.invoke(event.folder.id.toString(), event.folder.name)
            }

            is HomeEvent.NewFolderNameChanged -> {
                _uiState.update { it.copy(newFolderName = event.name) }
            }
            HomeEvent.ConfirmCreateFolder -> {
                val currentName = _uiState.value.newFolderName
                if (currentName.isNotBlank()) {
                    createFolder(currentName)
                }
            }
            HomeEvent.ConfirmRenameFolder -> {
                val folderToRename = _uiState.value.selectedFolder
                val newName = _uiState.value.newFolderName
                if (folderToRename != null && newName.isNotBlank()) {
                    renameFolder(folderToRename.id, newName)
                }
            }
            HomeEvent.ConfirmDeleteFolder -> {
                val folderToDelete = _uiState.value.selectedFolder
                if (folderToDelete != null) {
                    deleteFolder(folderToDelete.id)
                }
            }
            HomeEvent.QuizAiClicked -> {}
            HomeEvent.RingkasanClicked -> {}
            HomeEvent.ShowPopup -> {
                _uiState.update { it.copy(isPopupVisible = true) }
            }
            HomeEvent.DismissPopup -> {
                _uiState.update { it.copy(isPopupVisible = false) }
            }
            HomeEvent.ShowCreateFolderDialog -> {
                _uiState.update { it.copy(isPopupVisible = false, isCreateFolderDialogVisible = true) }
            }
            HomeEvent.DismissCreateFolderDialog -> {
                _uiState.update { it.copy(isCreateFolderDialogVisible = false, newFolderName = "") }
            }
            HomeEvent.DismissRenameFolderDialog -> {
                _uiState.update { it.copy(isRenameFolderDialogVisible = false, newFolderName = "", selectedFolder = null) }
            }
            HomeEvent.DismissDeleteFolderDialog -> {
                _uiState.update { it.copy(isDeleteFolderDialogVisible = false, selectedFolder = null) }
            }
            is HomeEvent.ShowFolderOptions -> {
                _uiState.update { it.copy(isFolderOptionsVisible = true, selectedFolder = event.folder) }
            }
            HomeEvent.DismissFolderOptions -> {
                _uiState.update { it.copy(isFolderOptionsVisible = false, selectedFolder = null) }
            }
            HomeEvent.RenameFolderClicked -> {
                val currentFolder = _uiState.value.selectedFolder
                _uiState.update {
                    it.copy(
                        isFolderOptionsVisible = false,
                        isRenameFolderDialogVisible = true,
                        newFolderName = currentFolder?.name ?: ""
                    )
                }
            }
            HomeEvent.DeleteFolderClicked -> {
                _uiState.update { it.copy(isFolderOptionsVisible = false, isDeleteFolderDialogVisible = true) }
            }
            HomeEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            HomeEvent.ClearSuccessMessage -> {
                _uiState.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            userPreferences.setLoggedIn(false)
            onNavigateToLogin?.invoke()
        }
    }
}