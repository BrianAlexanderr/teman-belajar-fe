package com.example.teman_belajar.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.teman_belajar.Register.ui.components.BackgroundDecoration
import com.example.teman_belajar.components.Navbar
import com.example.teman_belajar.theme.AppColors

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit
) {
    Scaffold(
        bottomBar = {
            Navbar(
                currentRoute = "home",
                onItemClick = {
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(HomeEvent.ShowPopup) },
                containerColor = AppColors.Purple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Folder")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            BackgroundDecoration(
                modifier = Modifier.fillMaxSize(),
                topColor = AppColors.Purple.copy(alpha = 0.7f),
                bottomColor = AppColors.Purple.copy(alpha = 0.4f)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Halo, ${uiState.userName}!!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "Siap untuk meraih target\nbelajarmu hari ini?",
                            fontSize = 16.sp,
                            color = AppColors.TextSecondary,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActionCard(
                                title = "Quiz AI",
                                icon = Icons.Outlined.Quiz,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HomeEvent.QuizAiClicked) }
                            )
                            ActionCard(
                                title = "Ringkasan",
                                icon = Icons.Outlined.AutoAwesome,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HomeEvent.RingkasanClicked) }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                            placeholder = { Text("Cari materi atau topik...", color = AppColors.TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Purple.copy(alpha = 0.5f),
                                unfocusedBorderColor = AppColors.InputBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Folder Materi Saya",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                items(uiState.folders) { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = { onEvent(HomeEvent.FolderClicked(folder)) },
                        onOptionsClick = { onEvent(HomeEvent.ShowFolderOptions(folder)) }
                    )
                }
                
                item(span = { GridItemSpan(3) }) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (uiState.isPopupVisible) {
        AddFolderPopup(
            onDismiss = { onEvent(HomeEvent.DismissPopup) },
            onCreateFolderClicked = { onEvent(HomeEvent.ShowCreateFolderDialog) }
        )
    }

    if (uiState.isCreateFolderDialogVisible) {
        CreateFolderNamingDialog(
            folderName = uiState.newFolderName,
            onNameChange = { onEvent(HomeEvent.NewFolderNameChanged(it)) },
            onDismiss = { onEvent(HomeEvent.DismissCreateFolderDialog) },
            onConfirm = { onEvent(HomeEvent.ConfirmCreateFolder) }
        )
    }

    if (uiState.isFolderOptionsVisible) {
        Dialog(onDismissRequest = { onEvent(HomeEvent.DismissFolderOptions) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                FolderOptionsContent(
                    onRename = { onEvent(HomeEvent.RenameFolderClicked) },
                    onDelete = { onEvent(HomeEvent.DeleteFolderClicked) },
                    onCancel = { onEvent(HomeEvent.DismissFolderOptions) }
                )
            }
        }
    }

    if (uiState.isRenameFolderDialogVisible) {
        RenameFolderDialog(
            folderName = uiState.newFolderName,
            onNameChange = { onEvent(HomeEvent.NewFolderNameChanged(it)) },
            onDismiss = { onEvent(HomeEvent.DismissRenameFolderDialog) },
            onConfirm = { onEvent(HomeEvent.ConfirmRenameFolder) }
        )
    }

    if (uiState.isDeleteFolderDialogVisible) {
        DeleteFolderConfirmationDialog(
            onDismiss = { onEvent(HomeEvent.DismissDeleteFolderDialog) },
            onConfirm = { onEvent(HomeEvent.ConfirmDeleteFolder) }
        )
    }
}

@Composable
fun DeleteFolderConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Hapus Folder",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Apakah anda yakin ingin menghapus folder beserta semua materinya? Tindakan ini tidak dapat dibatalkan.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iya, Hapus", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tidak, Batalkan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun RenameFolderDialog(
    folderName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(text = "Ganti Nama Folder", fontWeight = FontWeight.Bold, fontSize = 20.sp) 
        },
        text = {
            Column {
                Text(
                    text = "Masukkan nama baru untuk folder ini.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = folderName,
                    onValueChange = onNameChange,
                    placeholder = { Text("Nama Folder Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Purple,
                        unfocusedBorderColor = AppColors.InputBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                shape = RoundedCornerShape(12.dp),
                enabled = folderName.isNotBlank()
            ) {
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
fun FolderOptionsContent(
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "TINDAKAN FOLDER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRename() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF3E8FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AppColors.Purple,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Ganti Nama Folder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Ubah Nama Folder Ini",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDelete() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Hapus Folder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    text = "Pindahkan Folder ke tempat sampah",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Batal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AddFolderPopup(
    onDismiss: () -> Unit,
    onCreateFolderClicked: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateFolderClicked() }
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFF3F0EF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = null,
                            tint = AppColors.Purple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Folder Baru",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Buat kategori belajar baru",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color(0xFFEEEEEE)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tutup",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Purple
                    )
                }
            }
        }
    }
}

@Composable
fun CreateFolderNamingDialog(
    folderName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Nama Folder Baru", 
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ) 
        },
        text = {
            Column {
                Text(
                    text = "Masukkan nama untuk kategori belajar baru Anda.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = folderName,
                    onValueChange = onNameChange,
                    placeholder = { Text("Contoh: Matematika") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Purple,
                        unfocusedBorderColor = AppColors.InputBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                shape = RoundedCornerShape(12.dp),
                enabled = folderName.isNotBlank()
            ) {
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0EF)) 
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AppColors.Purple,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FolderCard(
    folder: FolderItem,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD9D9D9))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = folder.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = AppColors.TextSecondary
                )
            }
        }
    }
}
