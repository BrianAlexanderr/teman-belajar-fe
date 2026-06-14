package com.example.teman_belajar.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FolderItem(val id: Int, val name: String)

data class HomeUiState(
    val userName: String = "Pelajar",
    val folders: List<FolderItem> = listOf(
        FolderItem(1, "Biologi"),
        FolderItem(2, "Komputasi Fisika"),
        FolderItem(3, "Kalkulus")
    ),
    val isLoading: Boolean = false,
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
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var onNavigateToLogin: (() -> Unit)? = null

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LogoutClicked -> logout()
            is HomeEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is HomeEvent.FolderClicked -> {}
            is HomeEvent.NewFolderNameChanged -> {
                _uiState.update { it.copy(newFolderName = event.name) }
            }
            HomeEvent.ConfirmCreateFolder -> {
                val currentName = _uiState.value.newFolderName
                if (currentName.isNotBlank()) {
                    val newFolder = FolderItem(
                        id = (_uiState.value.folders.maxOfOrNull { it.id } ?: 0) + 1,
                        name = currentName
                    )
                    _uiState.update { state ->
                        state.copy(
                            folders = state.folders + newFolder,
                            isCreateFolderDialogVisible = false,
                            newFolderName = ""
                        )
                    }
                }
            }
            HomeEvent.ConfirmRenameFolder -> {
                val folderToRename = _uiState.value.selectedFolder
                val newName = _uiState.value.newFolderName
                if (folderToRename != null && newName.isNotBlank()) {
                    _uiState.update { state ->
                        state.copy(
                            folders = state.folders.map { 
                                if (it.id == folderToRename.id) it.copy(name = newName) else it 
                            },
                            isRenameFolderDialogVisible = false,
                            selectedFolder = null,
                            newFolderName = ""
                        )
                    }
                }
            }
            HomeEvent.ConfirmDeleteFolder -> {
                val folderToDelete = _uiState.value.selectedFolder
                if (folderToDelete != null) {
                    _uiState.update { state ->
                        state.copy(
                            folders = state.folders.filter { it.id != folderToDelete.id },
                            isDeleteFolderDialogVisible = false,
                            selectedFolder = null
                        )
                    }
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
        }
    }

    private fun logout() {
        viewModelScope.launch {
            userPreferences.setLoggedIn(false)
            onNavigateToLogin?.invoke()
        }
    }
}
