package com.example.teman_belajar.folderdetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class DummyFile(val id: Int, val name: String)

data class FolderDetailUiState(
    val folderId: String = "",
    val folderName: String = "",
    val searchQuery: String = "",

    val isAddFileMenuVisible: Boolean = false,
    val fileCounter: Int = 1,
    val newFileName: String = "",
    val files: List<DummyFile> = emptyList(),

    val isFileOptionsVisible: Boolean = false,
    val isRenameFileDialogVisible: Boolean = false,
    val isDeleteFileDialogVisible: Boolean = false,
    val selectedFile: DummyFile? = null
)

sealed class FolderDetailEvent {
    object NavigateBack : FolderDetailEvent()
    data class SearchQueryChanged(val query: String) : FolderDetailEvent()
    object GenerateQuizClicked : FolderDetailEvent()
    object SmartSummaryClicked : FolderDetailEvent()

    object AddMateriClicked : FolderDetailEvent()
    object DismissAddFileMenu : FolderDetailEvent()
    object CameraOptionClicked : FolderDetailEvent()
    object DeviceOptionClicked : FolderDetailEvent()
    data class NewFileNameChanged(val name: String) : FolderDetailEvent()

    data class ShowFileOptions(val file: DummyFile) : FolderDetailEvent()
    object DismissFileOptions : FolderDetailEvent()
    object RenameFileClicked : FolderDetailEvent()
    object DeleteFileClicked : FolderDetailEvent()
    object DismissRenameFileDialog : FolderDetailEvent()
    object DismissDeleteFileDialog : FolderDetailEvent()
    object ConfirmRenameFile : FolderDetailEvent()
    object ConfirmDeleteFile : FolderDetailEvent()
}

class FolderDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FolderDetailUiState())
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null

    fun setFolderData(id: String, name: String) {
        _uiState.update { it.copy(folderId = id, folderName = name) }
    }

    fun onEvent(event: FolderDetailEvent) {
        when (event) {
            FolderDetailEvent.NavigateBack -> {
                onNavigateBack?.invoke()
            }
            is FolderDetailEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }

            FolderDetailEvent.AddMateriClicked -> {
                _uiState.update { it.copy(isAddFileMenuVisible = true) }
            }
            FolderDetailEvent.DismissAddFileMenu -> {
                _uiState.update { it.copy(isAddFileMenuVisible = false) }
            }
            FolderDetailEvent.CameraOptionClicked -> {
                _uiState.update { it.copy(isAddFileMenuVisible = false) }
            }
            FolderDetailEvent.DeviceOptionClicked -> {
                _uiState.update { state ->
                    val newDummyFile = DummyFile(
                        id = Random.nextInt(100, 10000),
                        name = "File${state.fileCounter}"
                    )

                    state.copy(
                        isAddFileMenuVisible = false,
                        files = state.files + newDummyFile,
                        fileCounter = state.fileCounter + 1
                    )
                }
            }

            is FolderDetailEvent.NewFileNameChanged -> {
                _uiState.update { it.copy(newFileName = event.name) }
            }
            is FolderDetailEvent.ShowFileOptions -> {
                _uiState.update { it.copy(isFileOptionsVisible = true, selectedFile = event.file) }
            }
            FolderDetailEvent.DismissFileOptions -> {
                _uiState.update { it.copy(isFileOptionsVisible = false, selectedFile = null) }
            }
            FolderDetailEvent.RenameFileClicked -> {
                val currentFile = _uiState.value.selectedFile
                _uiState.update {
                    it.copy(
                        isFileOptionsVisible = false,
                        isRenameFileDialogVisible = true,
                        newFileName = currentFile?.name ?: ""
                    )
                }
            }
            FolderDetailEvent.DeleteFileClicked -> {
                _uiState.update { it.copy(isFileOptionsVisible = false, isDeleteFileDialogVisible = true) }
            }
            FolderDetailEvent.DismissRenameFileDialog -> {
                _uiState.update { it.copy(isRenameFileDialogVisible = false, newFileName = "", selectedFile = null) }
            }
            FolderDetailEvent.DismissDeleteFileDialog -> {
                _uiState.update { it.copy(isDeleteFileDialogVisible = false, selectedFile = null) }
            }

            FolderDetailEvent.ConfirmRenameFile -> {
                val fileToRename = _uiState.value.selectedFile
                val newName = _uiState.value.newFileName

                if (fileToRename != null && newName.isNotBlank()) {
                    _uiState.update { state ->
                        val updatedFiles = state.files.map { file ->
                            if (file.id == fileToRename.id) file.copy(name = newName) else file
                        }

                        state.copy(
                            files = updatedFiles,
                            isRenameFileDialogVisible = false,
                            newFileName = "",
                            selectedFile = null
                        )
                    }
                }
            }

            FolderDetailEvent.ConfirmDeleteFile -> {
                val fileToDelete = _uiState.value.selectedFile
                if (fileToDelete != null) {
                    _uiState.update { state ->
                        val updatedFiles = state.files.filterNot { it.id == fileToDelete.id }

                        state.copy(
                            files = updatedFiles,
                            isDeleteFileDialogVisible = false,
                            selectedFile = null
                        )
                    }
                }
            }

            FolderDetailEvent.GenerateQuizClicked -> {
                // Placeholder generate quiz
            }
            FolderDetailEvent.SmartSummaryClicked -> {
                // Placeholder summary
            }
        }
    }
}