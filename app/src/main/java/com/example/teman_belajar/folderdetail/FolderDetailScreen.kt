package com.example.teman_belajar.folderdetail

import com.example.teman_belajar.components.ActionMenuItem
import com.example.teman_belajar.components.ActionSelectionDialog
import com.example.teman_belajar.components.ConfirmationDialog
import com.example.teman_belajar.components.TextInputDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    uiState: FolderDetailUiState,
    onEvent: (FolderDetailEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onEvent(FolderDetailEvent.NavigateBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = uiState.folderName.ifEmpty { "Folder" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clickable { onEvent(FolderDetailEvent.GenerateQuizClicked) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFE6FF)),
                border = BorderStroke(1.dp, Color(0xFFD8C4FF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = "Generate Quiz",
                        tint = AppColors.Purple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Generate Quiz",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Purple,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Latih pemahamanmu dari materi ini.",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clickable { onEvent(FolderDetailEvent.SmartSummaryClicked) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Smart Summary",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Smart Summary",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ringkasan poin penting secara instan.",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onEvent(FolderDetailEvent.SearchQueryChanged(it)) },
            placeholder = { Text("Cari materi atau topik...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Purple,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Materi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            IconButton(
                onClick = { onEvent(FolderDetailEvent.AddMateriClicked) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Materi",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada materi.\nKlik '+' untuk menambahkan.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(uiState.files) { file ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD1D5DB))
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = file.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onEvent(FolderDetailEvent.ShowFileOptions(file)) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isAddFileMenuVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(FolderDetailEvent.DismissAddFileMenu) },
            items = listOf(
                ActionMenuItem(
                    title = "Kamera",
                    subtitle = "Scan catatan fisikmu",
                    icon = Icons.Outlined.CameraAlt,
                    iconTint = Color.White,
                    iconBgColor = AppColors.Purple,
                    onClick = { onEvent(FolderDetailEvent.CameraOptionClicked) }
                ),
                ActionMenuItem(
                    title = "Tambah dari Perangkat",
                    subtitle = "Unggah dokumen PDF atau Gambar",
                    icon = Icons.Outlined.UploadFile,
                    iconTint = AppColors.Purple,
                    iconBgColor = Color(0xFFF3F0EF),
                    onClick = { onEvent(FolderDetailEvent.DeviceOptionClicked) }
                )
            )
        )
    }

    if (uiState.isFileOptionsVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(FolderDetailEvent.DismissFileOptions) },
            title = "TINDAKAN FILE",
            items = listOf(
                ActionMenuItem(
                    title = "Ganti Nama File",
                    subtitle = "Ubah nama file ini",
                    icon = Icons.Default.Edit,
                    iconBgColor = Color(0xFFF3E8FF),
                    onClick = { onEvent(FolderDetailEvent.RenameFileClicked) }
                ),
                ActionMenuItem(
                    title = "Hapus File",
                    subtitle = "Pindahkan file ke tempat sampah",
                    icon = Icons.Default.DeleteOutline,
                    iconTint = Color.Red,
                    iconBgColor = Color(0xFFFEE2E2),
                    titleColor = Color.Red,
                    onClick = { onEvent(FolderDetailEvent.DeleteFileClicked) }
                )
            )
        )
    }

    if (uiState.isRenameFileDialogVisible) {
        TextInputDialog(
            title = "Ganti Nama File",
            subtitle = "Masukkan nama baru untuk file ini.",
            value = uiState.newFileName,
            onValueChange = { onEvent(FolderDetailEvent.NewFileNameChanged(it)) },
            placeholder = "Nama File Baru",
            onDismiss = { onEvent(FolderDetailEvent.DismissRenameFileDialog) },
            onConfirm = { onEvent(FolderDetailEvent.ConfirmRenameFile) }
        )
    }

    if (uiState.isDeleteFileDialogVisible) {
        ConfirmationDialog(
            title = "Hapus File",
            description = "Apakah Anda yakin ingin menghapus file ini? Tindakan ini tidak dapat dibatalkan.",
            icon = Icons.Outlined.DeleteForever,
            confirmButtonText = "Iya, Hapus",
            dismissButtonText = "Batal",
            onDismiss = { onEvent(FolderDetailEvent.DismissDeleteFileDialog) },
            onConfirm = { onEvent(FolderDetailEvent.ConfirmDeleteFile) }
        )
    }
}