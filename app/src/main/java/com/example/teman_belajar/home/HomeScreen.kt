package com.example.teman_belajar.home

import android.widget.Toast
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.teman_belajar.register.ui.components.BackgroundDecoration
import com.example.teman_belajar.components.*
import com.example.teman_belajar.theme.AppColors

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Fetch folders every time the screen comes to foreground (Resume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onEvent(HomeEvent.FetchFolders)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            onEvent(HomeEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { successMsg ->
            Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
            onEvent(HomeEvent.ClearSuccessMessage)
        }
    }

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
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
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
                            color = AppColors.TextPrimary
                        )
                    }
                }

                if (uiState.isLoading) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.Purple)
                        }
                    }
                }

                else if (uiState.folders.isEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        EmptyFolderState()
                    }
                }

                else {
                    items(uiState.folders) { folder ->
                        FolderCard(
                            folder = folder,
                            onClick = { onEvent(HomeEvent.FolderClicked(folder)) },
                            onOptionsClick = { onEvent(HomeEvent.ShowFolderOptions(folder)) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isPopupVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(HomeEvent.DismissPopup) },
            items = listOf(
                ActionMenuItem(
                    title = "Folder Baru",
                    subtitle = "Buat kategori belajar baru",
                    icon = Icons.Outlined.CreateNewFolder,
                    onClick = { onEvent(HomeEvent.ShowCreateFolderDialog) }
                )
            )
        )
    }

    if (uiState.isCreateFolderDialogVisible) {
        TextInputDialog(
            title = "Nama Folder Baru",
            subtitle = "Masukkan nama untuk kategori belajar baru Anda.",
            value = uiState.newFolderName,
            onValueChange = { onEvent(HomeEvent.NewFolderNameChanged(it)) },
            placeholder = "Contoh: Matematika",
            onDismiss = { onEvent(HomeEvent.DismissCreateFolderDialog) },
            onConfirm = { onEvent(HomeEvent.ConfirmCreateFolder) }
        )
    }

    if (uiState.isFolderOptionsVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(HomeEvent.DismissFolderOptions) },
            title = "TINDAKAN FOLDER",
            items = listOf(
                ActionMenuItem(
                    title = "Ganti Nama Folder",
                    subtitle = "Ubah Nama Folder Ini",
                    icon = Icons.Default.Edit,
                    iconBgColor = Color(0xFFF3E8FF),
                    onClick = { onEvent(HomeEvent.RenameFolderClicked) }
                ),
                ActionMenuItem(
                    title = "Hapus Folder",
                    subtitle = "Pindahkan Folder ke tempat sampah",
                    icon = Icons.Default.DeleteOutline,
                    iconTint = Color.Red,
                    iconBgColor = Color(0xFFFEE2E2),
                    titleColor = Color.Red,
                    onClick = { onEvent(HomeEvent.DeleteFolderClicked) }
                )
            )
        )
    }

    if (uiState.isRenameFolderDialogVisible) {
        TextInputDialog(
            title = "Ganti Nama Folder",
            subtitle = "Masukkan nama baru untuk folder ini.",
            value = uiState.newFolderName,
            onValueChange = { onEvent(HomeEvent.NewFolderNameChanged(it)) },
            placeholder = "Nama Folder Baru",
            onDismiss = { onEvent(HomeEvent.DismissRenameFolderDialog) },
            onConfirm = { onEvent(HomeEvent.ConfirmRenameFolder) }
        )
    }

    if (uiState.isDeleteFolderDialogVisible) {
        ConfirmationDialog(
            title = "Hapus Folder",
            description = "Apakah anda yakin ingin menghapus folder beserta semua materinya? Tindakan ini tidak dapat dibatalkan.",
            icon = Icons.Outlined.DeleteForever,
            confirmButtonText = "Iya, Hapus",
            dismissButtonText = "Tidak, Batalkan",
            onDismiss = { onEvent(HomeEvent.DismissDeleteFolderDialog) },
            onConfirm = { onEvent(HomeEvent.ConfirmDeleteFolder) }
        )
    }
}

@Composable
fun EmptyFolderState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CreateNewFolder,
            contentDescription = "Folder Kosong",
            tint = AppColors.TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada folder materi",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary.copy(alpha = 0.7f)
        )
        Text(
            text = "Klik tombol + di bawah untuk membuat folder baru",
            fontSize = 14.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
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

class FolderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val tabWidth = size.width * 0.45f
            val tabHeight = size.height * 0.18f
            val cornerRadius = with(density) { 12.dp.toPx() }

            moveTo(0f, cornerRadius)
            quadraticTo(0f, 0f, cornerRadius, 0f)

            lineTo(tabWidth - cornerRadius, 0f)

            cubicTo(
                tabWidth, 0f,
                tabWidth, tabHeight,
                tabWidth + cornerRadius, tabHeight
            )

            lineTo(size.width - cornerRadius, tabHeight)
            quadraticTo(size.width, tabHeight, size.width, tabHeight + cornerRadius)

            lineTo(size.width, size.height - cornerRadius)
            quadraticTo(size.width, size.height, size.width - cornerRadius, size.height)

            lineTo(cornerRadius, size.height)
            quadraticTo(0f, size.height, 0f, size.height - cornerRadius)

            close()
        }
        return Outline.Generic(path)
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
                .clip(FolderShape())
                .background(AppColors.Purple.copy(alpha = 0.5f))
        ) {
        }

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