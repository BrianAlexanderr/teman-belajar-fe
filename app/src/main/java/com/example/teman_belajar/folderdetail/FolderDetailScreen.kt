package com.example.teman_belajar.folderdetail

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.teman_belajar.components.ActionMenuItem
import com.example.teman_belajar.components.ActionSelectionDialog
import com.example.teman_belajar.components.ConfirmationDialog
import com.example.teman_belajar.components.TextInputDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.teman_belajar.R
import com.example.teman_belajar.theme.AppColors
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import kotlin.math.min

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return try {
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun downloadFile(context: Context, url: String, fileName: String) {
    try {
        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setDescription("Sedang mengunduh materi...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Mulai mengunduh...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal mengunduh file", Toast.LENGTH_SHORT).show()
    }
}

fun openFile(context: Context, uriString: String?, mimeType: String) {
    if (uriString.isNullOrEmpty()) {
        Toast.makeText(context, "URL kosong", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        when {
            mimeType == "application/pdf" -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uriString.toUri(), "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            }
            mimeType == "application/msword" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                    mimeType == "application/vnd.ms-powerpoint" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                val viewerUrl = "https://docs.google.com/gview?embedded=true&url=" + URLEncoder.encode(uriString, "UTF-8")
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("url", viewerUrl)
                context.startActivity(intent)
            }
            mimeType.startsWith("image") -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uriString.toUri(), mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            }
            else -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uriString.toUri(), mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal membuka file", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    viewModel: FolderDetailViewModel,
    uiState: FolderDetailUiState,
    onEvent: (FolderDetailEvent) -> Unit
) {
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.onOpenFile = { url, mimeType ->
            openFile(context, url, mimeType)
        }
        viewModel.onDownloadFile = { url, fileName ->
            downloadFile(context, url, fileName)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveBitmapToInternalStorage(context, bitmap)
            onEvent(FolderDetailEvent.FileAdded(
                name = "Camera_Capture_${System.currentTimeMillis()}.jpg",
                mimeType = "image/jpeg",
                uri = uri?.toString()
            ))
            Toast.makeText(context, "Foto berhasil diambil!", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_LONG).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(it) ?: "application/octet-stream"
            var fileName = "New_File_${uiState.fileCounter}"
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            onEvent(FolderDetailEvent.FileAdded(name = fileName, mimeType = mimeType, uri = it.toString()))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isSummarySelectionMode) "Pilih Materi (${uiState.selectedMaterialIds.size})" else uiState.folderName.ifEmpty { "Folder" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isSummarySelectionMode) onEvent(FolderDetailEvent.CancelSummarySelection)
                        else onEvent(FolderDetailEvent.NavigateBack)
                    }) {
                        Icon(
                            imageVector = if (uiState.isSummarySelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (!uiState.isSummarySelectionMode) {
                        IconButton(onClick = { onEvent(FolderDetailEvent.ShowFolderOptions) }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading && uiState.allFiles.isNotEmpty(),
                onRefresh = { onEvent(FolderDetailEvent.Refresh) },
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = uiState.isLoading && uiState.allFiles.isNotEmpty(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        color = AppColors.Purple,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                if (uiState.isLoading && uiState.allFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Purple)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = if (uiState.isSummarySelectionMode) 120.dp else 24.dp)
                    ) {
                        item {
                            val isDark = MaterialTheme.colorScheme.surface != Color.White
                            val bannerBg = if (uiState.isSummarySelectionMode) {
                                Brush.horizontalGradient(listOf(AppColors.Purple, AppColors.Purple))
                            } else {
                                if (isDark) {
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                } else {
                                    Brush.horizontalGradient(listOf(AppColors.PurpleLight, AppColors.DecorationBot))
                                }
                            }

                            Card(
                                onClick = {
                                    if (uiState.isSummarySelectionMode) {
                                        onEvent(FolderDetailEvent.CancelSummarySelection)
                                    } else {
                                        onEvent(FolderDetailEvent.SmartSummaryClicked)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = if (uiState.isSummarySelectionMode) null else BorderStroke(1.dp, AppColors.PurpleDot.copy(alpha = 0.5f)),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Box(modifier = Modifier.background(bannerBg).padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(48.dp).clip(CircleShape).background(if (uiState.isSummarySelectionMode) Color.White else MaterialTheme.colorScheme.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.AutoAwesome, null, tint = AppColors.Purple, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (uiState.isSummarySelectionMode) "Batalkan Pilihan" else "Ringkasan AI",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = if (uiState.isSummarySelectionMode) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (uiState.isSummarySelectionMode) "Klik kembali untuk membatalkan materi." else "Ringkasan materi belajar instan.",
                                                fontSize = 12.sp,
                                                color = if (uiState.isSummarySelectionMode) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        item {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { onEvent(FolderDetailEvent.SearchQueryChanged(it)) },
                                placeholder = { Text("Cari materi...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(32.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Purple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        if (!uiState.isSummarySelectionMode && uiState.smartSummaries.isNotEmpty()) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Ringkasan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Outlined.AutoAwesome, null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    items(uiState.smartSummaries) { file ->
                                        SmartSummaryCard(file, Modifier.width(220.dp).height(100.dp), { onEvent(FolderDetailEvent.FileClicked(file)) }, { onEvent(FolderDetailEvent.ShowFileOptions(file)) })
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Materi Belajar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (!uiState.isSummarySelectionMode) {
                                    IconButton(onClick = { onEvent(FolderDetailEvent.AddMateriClicked) }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Materi", tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (uiState.materials.isNotEmpty()) {
                            items(uiState.materials.chunkedList(2)) { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    rowItems.forEach { file ->
                                        CourseMaterialCard(
                                            file = file,
                                            modifier = Modifier.weight(1f),
                                            isSelectionMode = uiState.isSummarySelectionMode,
                                            isSelected = uiState.selectedMaterialIds.contains(file.id),
                                            onClick = { onEvent(FolderDetailEvent.FileClicked(file)) },
                                            onOptions = { onEvent(FolderDetailEvent.ShowFileOptions(file)) },
                                            onDownload = { onEvent(FolderDetailEvent.DownloadFileClicked(file)) }
                                        )
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(if (uiState.searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Outlined.FolderOpen, null, Modifier.size(80.dp), MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(Modifier.height(16.dp))
                                    Text(if (uiState.searchQuery.isNotEmpty()) "Tidak ditemukan" else "Folder kosong", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Belum ada materi di sini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.isSummarySelectionMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 64.dp, start = 24.dp, end = 24.dp).imePadding()
            ) {
                Button(
                    onClick = { onEvent(FolderDetailEvent.ConfirmSmartSummary) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple, disabledContainerColor = MaterialTheme.colorScheme.outlineVariant),
                    enabled = uiState.selectedMaterialIds.isNotEmpty() && !uiState.isGeneratingSummary
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AutoAwesome, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Buat Smart Summary (${uiState.selectedMaterialIds.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (uiState.isGeneratingSummary) {
        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = AppColors.Purple, strokeWidth = 4.dp, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Sedang membuat ringkasan...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Harap tunggu sebentar.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    subtitle = "Scan catatan",
                    icon = Icons.Outlined.CameraAlt,
                    onClick = {
                        onEvent(FolderDetailEvent.DismissAddFileMenu)
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) takePictureLauncher.launch(null) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    iconTint = Color.White,
                    iconBgColor = AppColors.Purple
                ),
                ActionMenuItem(
                    title = "Unggah",
                    subtitle = "Pilih dari perangkat",
                    icon = Icons.Outlined.UploadFile,
                    onClick = { onEvent(FolderDetailEvent.DismissAddFileMenu); filePickerLauncher.launch("*/*") },
                    iconTint = AppColors.Purple,
                    iconBgColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        )
    }

    if (uiState.isFileOptionsVisible) {
        ActionSelectionDialog(onDismiss = { onEvent(FolderDetailEvent.DismissFileOptions) }, title = "TINDAKAN FILE", items = listOf(
            ActionMenuItem(
                title = "Ganti Nama",
                subtitle = "Ubah nama file",
                icon = Icons.Default.Edit,
                onClick = { onEvent(FolderDetailEvent.RenameFileClicked) },
                iconBgColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            ActionMenuItem(
                title = "Hapus",
                subtitle = "Hapus file ini",
                icon = Icons.Default.DeleteOutline,
                onClick = { onEvent(FolderDetailEvent.DeleteFileClicked) },
                iconTint = AppColors.Error,
                iconBgColor = AppColors.ErrorSurface,
                titleColor = AppColors.Error
            )
        ))
    }

    if (uiState.isFolderOptionsVisible) {
        ActionSelectionDialog(onDismiss = { onEvent(FolderDetailEvent.DismissFolderOptions) }, title = "TINDAKAN FOLDER", items = listOf(
            ActionMenuItem(
                title = "Ganti Nama",
                subtitle = "Ubah nama folder",
                icon = Icons.Default.Edit,
                onClick = { onEvent(FolderDetailEvent.RenameFolderClicked) },
                iconBgColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            ActionMenuItem(
                title = "Hapus",
                subtitle = "Hapus folder",
                icon = Icons.Default.DeleteOutline,
                onClick = { onEvent(FolderDetailEvent.DeleteFolderClicked) },
                iconTint = AppColors.Error,
                iconBgColor = AppColors.ErrorSurface,
                titleColor = AppColors.Error
            )
        ))
    }

    if (uiState.isRenameFileDialogVisible) {
        TextInputDialog("Ganti Nama File", "Masukkan nama baru.", uiState.newFileName, { onEvent(FolderDetailEvent.NewFileNameChanged(it)) }, "Nama File", { onEvent(FolderDetailEvent.DismissRenameFileDialog) }, { onEvent(FolderDetailEvent.ConfirmRenameFile) })
    }

    if (uiState.isRenameFolderDialogVisible) {
        TextInputDialog("Ganti Nama Folder", "Masukkan nama baru.", uiState.newFolderName, { onEvent(FolderDetailEvent.NewFolderNameChanged(it)) }, "Nama Folder", { onEvent(FolderDetailEvent.DismissRenameFolderDialog) }, { onEvent(FolderDetailEvent.ConfirmRenameFolder) })
    }

    if (uiState.isDeleteFileDialogVisible) {
        ConfirmationDialog("Hapus File", "Yakin ingin menghapus?", Icons.Outlined.DeleteForever, AppColors.Error, AppColors.ErrorSurface, "Hapus", AppColors.Error, "Batal", { onEvent(FolderDetailEvent.DismissDeleteFileDialog) }, { onEvent(FolderDetailEvent.ConfirmDeleteFile) })
    }

    if (uiState.isDeleteFolderDialogVisible) {
        ConfirmationDialog("Hapus Folder", "Hapus folder beserta isinya?", Icons.Outlined.DeleteForever, AppColors.Error, AppColors.ErrorSurface, "Hapus", AppColors.Error, "Batal", { onEvent(FolderDetailEvent.DismissDeleteFolderDialog) }, { onEvent(FolderDetailEvent.ConfirmDeleteFolder) })
    }
}

@Composable
private fun SmartSummaryCard(file: DummyFile, modifier: Modifier = Modifier, onClick: () -> Unit, onOptions: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, AppColors.Purple.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(file.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 24.dp))
                Spacer(Modifier.height(4.dp))
                Text(file.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onOptions, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun CourseMaterialCard(
    file: DummyFile, 
    modifier: Modifier = Modifier, 
    isSelectionMode: Boolean = false, 
    isSelected: Boolean = false, 
    onClick: () -> Unit, 
    onOptions: () -> Unit, 
    onDownload: () -> Unit
) {
    val isImage = file.mimeType.startsWith("image") || file.name.lowercase().let { it.endsWith(".jpg") || it.endsWith(".png") }
    Card(
        modifier = modifier.height(110.dp).clickable { onClick() }, 
        shape = RoundedCornerShape(12.dp), 
        colors = CardDefaults.cardColors(containerColor = if (isSelectionMode && isSelected) AppColors.Purple.copy(alpha = 0.08f) else if (isImage) Color.Transparent else MaterialTheme.colorScheme.surface), 
        border = BorderStroke(if (isSelectionMode && isSelected) 1.5.dp else 1.dp, if (isSelectionMode && isSelected) AppColors.Purple else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isImage) {
                AsyncImage(model = file.uri ?: R.drawable.file, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))).padding(8.dp)) {
                    Column {
                        Text(file.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("IMG", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            } else {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Image(painter = painterResource(if (file.mimeType.contains("pdf")) R.drawable.file else R.drawable.doc), contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (isSelectionMode) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(24.dp).clip(CircleShape).background(if (isSelected) AppColors.Purple else Color.White.copy(alpha = 0.8f)).border(1.5.dp, if (isSelected) AppColors.Purple else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                    if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            } else {
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDownload, modifier = Modifier.size(28.dp)) { Icon(Icons.Outlined.FileDownload, null, tint = if (isImage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onOptions, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.MoreVert, null, tint = if (isImage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

private fun <T> List<T>.chunkedList(size: Int): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var i = 0
    while (i < this.size) {
        result.add(subList(i, min(i + size, this.size)))
        i += size
    }
    return result
}
