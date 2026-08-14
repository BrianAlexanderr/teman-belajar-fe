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

/**
 * Menggunakan DownloadManager untuk mengunduh file langsung ke folder Downloads perangkat.
 */
fun downloadFile(context: Context, url: String, fileName: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Surface)
                .systemBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (uiState.isSummarySelectionMode) onEvent(FolderDetailEvent.CancelSummarySelection)
                    else onEvent(FolderDetailEvent.NavigateBack)
                }) {
                    Icon(
                        imageVector = if (uiState.isSummarySelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isSummarySelectionMode) "Pilih Materi (${uiState.selectedMaterialIds.size})" else uiState.folderName.ifEmpty { "Folder" },
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!uiState.isSummarySelectionMode) {
                    IconButton(onClick = { onEvent(FolderDetailEvent.ShowFolderOptions) }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = AppColors.TextPrimary)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isLoading && uiState.allFiles.isNotEmpty(),
                onRefresh = { onEvent(FolderDetailEvent.Refresh) },
                state = pullToRefreshState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = uiState.isLoading && uiState.allFiles.isNotEmpty(),
                        containerColor = AppColors.Surface,
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
                            val bannerBg = if (uiState.isSummarySelectionMode) {
                                Brush.horizontalGradient(listOf(AppColors.Purple, AppColors.Purple))
                            } else {
                                Brush.horizontalGradient(listOf(AppColors.PurpleLight, AppColors.DecorationBot))
                            }

                            Card(
                                onClick = {
                                    if (uiState.isSummarySelectionMode) {
                                        onEvent(FolderDetailEvent.CancelSummarySelection)
                                    } else {
                                        onEvent(FolderDetailEvent.SmartSummaryClicked)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = if (uiState.isSummarySelectionMode) null else BorderStroke(1.dp, AppColors.PurpleDot),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (uiState.isSummarySelectionMode) 4.dp else 0.dp)
                            ) {
                                Box(modifier = Modifier.background(bannerBg).padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(AppColors.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                tint = AppColors.Purple,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (uiState.isSummarySelectionMode) "Batalkan Pilihan" else "Ringkasan AI",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = if (uiState.isSummarySelectionMode) AppColors.White else AppColors.TextPrimary
                                            )
                                            Text(
                                                text = if (uiState.isSummarySelectionMode) "Klik kembali untuk membatalkan pemilihan materi." else "Dapatkan ringkasan materi belajar secara instan.",
                                                fontSize = 12.sp,
                                                color = if (uiState.isSummarySelectionMode) AppColors.White.copy(alpha = 0.9f) else AppColors.TextSecondary,
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
                                placeholder = { Text("Cari materi atau topik...", color = AppColors.TextSecondary, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(32.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Purple,
                                    unfocusedBorderColor = AppColors.InputBorder,
                                    focusedContainerColor = AppColors.White,
                                    unfocusedContainerColor = AppColors.White
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        if (!uiState.isSummarySelectionMode && uiState.smartSummaries.isNotEmpty()) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Ringkasan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(uiState.smartSummaries) { file ->
                                        SmartSummaryCard(
                                            file = file,
                                            modifier = Modifier.width(220.dp).height(100.dp),
                                            onClick = { onEvent(FolderDetailEvent.FileClicked(file)) },
                                            onOptions = { onEvent(FolderDetailEvent.ShowFileOptions(file)) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Materi Belajar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                if (!uiState.isSummarySelectionMode) {
                                    IconButton(onClick = { onEvent(FolderDetailEvent.AddMateriClicked) }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Materi", tint = AppColors.TextPrimary)
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
                                            isSelectionMode = uiState.isSummarySelectionMode,
                                            isSelected = uiState.selectedMaterialIds.contains(file.id),
                                            modifier = Modifier.weight(1f),
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
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(80.dp), tint = AppColors.InputBorder)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(text = "Pencarian tidak ditemukan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                        Text(text = "Coba kata kunci lain atau periksa ejaanmu.", color = AppColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                                    } else {
                                        Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(80.dp), tint = AppColors.InputBorder)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(text = "Folder kosong", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                        Text(text = "Belum ada materi atau ringkasan di sini.", color = AppColors.TextSecondary, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = uiState.isSummarySelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 64.dp, start = 24.dp, end = 24.dp)
                .imePadding()
        ) {
            Button(
                onClick = { onEvent(FolderDetailEvent.ConfirmSmartSummary) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Purple,
                    disabledContainerColor = AppColors.InputBorder
                ),
                enabled = uiState.selectedMaterialIds.isNotEmpty() && !uiState.isGeneratingSummary
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Smart Summary (${uiState.selectedMaterialIds.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (uiState.isGeneratingSummary) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.Purple,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Sedang membuat ringkasan...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Harap tunggu sebentar. AI sedang memproses materi.",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (uiState.isAddFileMenuVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(FolderDetailEvent.DismissAddFileMenu) },
            items = listOf(
                ActionMenuItem(
                    title = "Kamera", subtitle = "Scan catatan fisikmu", icon = Icons.Outlined.CameraAlt, iconTint = AppColors.White, iconBgColor = AppColors.Purple,
                    onClick = {
                        onEvent(FolderDetailEvent.DismissAddFileMenu)
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) takePictureLauncher.launch(null) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ),
                ActionMenuItem(
                    title = "Tambah dari Perangkat", subtitle = "Unggah dokumen PDF atau Gambar", icon = Icons.Outlined.UploadFile, iconTint = AppColors.Purple, iconBgColor = AppColors.BgColor,
                    onClick = { onEvent(FolderDetailEvent.DismissAddFileMenu); filePickerLauncher.launch("*/*") }
                )
            )
        )
    }

    if (uiState.isFileOptionsVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(FolderDetailEvent.DismissFileOptions) },
            title = "TINDAKAN FILE",
            items = listOf(
                ActionMenuItem(title = "Ganti Nama File", subtitle = "Ubah nama file ini", icon = Icons.Default.Edit, iconBgColor = AppColors.DecorationBot, onClick = { onEvent(FolderDetailEvent.RenameFileClicked) }),
                ActionMenuItem(title = "Hapus File", subtitle = "Pindahkan file ke tempat sampah", icon = Icons.Default.DeleteOutline, iconTint = AppColors.Error, iconBgColor = AppColors.ErrorSurface, titleColor = AppColors.Error, onClick = { onEvent(FolderDetailEvent.DeleteFileClicked) })
            )
        )
    }

    if (uiState.isFolderOptionsVisible) {
        ActionSelectionDialog(
            onDismiss = { onEvent(FolderDetailEvent.DismissFolderOptions) },
            title = "TINDAKAN FOLDER",
            items = listOf(
                ActionMenuItem(title = "Ganti Nama Folder", subtitle = "Ubah nama folder ini", icon = Icons.Default.Edit, iconBgColor = AppColors.DecorationBot, onClick = { onEvent(FolderDetailEvent.RenameFolderClicked) }),
                ActionMenuItem(title = "Hapus Folder", subtitle = "Hapus folder beserta isinya", icon = Icons.Default.DeleteOutline, iconTint = AppColors.Error, iconBgColor = AppColors.ErrorSurface, titleColor = AppColors.Error, onClick = { onEvent(FolderDetailEvent.DeleteFolderClicked) })
            )
        )
    }

    if (uiState.isRenameFileDialogVisible) {
        TextInputDialog(
            title = "Ganti Nama File", subtitle = "Masukkan nama baru untuk file ini.", value = uiState.newFileName,
            onValueChange = { onEvent(FolderDetailEvent.NewFileNameChanged(it)) }, placeholder = "Nama File Baru",
            onDismiss = { onEvent(FolderDetailEvent.DismissRenameFileDialog) }, onConfirm = { onEvent(FolderDetailEvent.ConfirmRenameFile) }
        )
    }

    if (uiState.isRenameFolderDialogVisible) {
        TextInputDialog(
            title = "Ganti Nama Folder", subtitle = "Masukkan nama baru untuk folder ini.", value = uiState.newFolderName,
            onValueChange = { onEvent(FolderDetailEvent.NewFolderNameChanged(it)) }, placeholder = "Nama Folder Baru",
            onDismiss = { onEvent(FolderDetailEvent.DismissRenameFolderDialog) }, onConfirm = { onEvent(FolderDetailEvent.ConfirmRenameFolder) }
        )
    }

    if (uiState.isDeleteFileDialogVisible) {
        ConfirmationDialog(
            title = "Hapus File", description = "Apakah Anda yakin ingin menghapus file ini?", icon = Icons.Outlined.DeleteForever,
            confirmButtonText = "Iya, Hapus", dismissButtonText = "Batal",
            onDismiss = { onEvent(FolderDetailEvent.DismissDeleteFileDialog) }, onConfirm = { onEvent(FolderDetailEvent.ConfirmDeleteFile) }
        )
    }

    if (uiState.isDeleteFolderDialogVisible) {
        ConfirmationDialog(
            title = "Hapus Folder", description = "Apakah Anda yakin ingin menghapus folder ini? Seluruh materi di dalamnya akan ikut terhapus.", icon = Icons.Outlined.DeleteForever,
            confirmButtonText = "Iya, Hapus", dismissButtonText = "Batal",
            onDismiss = { onEvent(FolderDetailEvent.DismissDeleteFolderDialog) }, onConfirm = { onEvent(FolderDetailEvent.ConfirmDeleteFolder) }
        )
    }
}

@Composable
private fun SmartSummaryCard(
    file: DummyFile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onOptions: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.DecorationBot),
        border = BorderStroke(1.dp, AppColors.Purple)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 24.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = file.description,
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onOptions,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 2.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CourseMaterialCard(
    file: DummyFile,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onOptions: () -> Unit,
    onDownload: () -> Unit
) {
    val nameLower = file.name.lowercase()
    val isImage = file.mimeType.startsWith("image", ignoreCase = true) ||
            nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") ||
            nameLower.endsWith(".png") || nameLower.endsWith(".webp") ||
            FileType.fromMimeType(file.mimeType) == FileType.IMAGE

    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectionMode && isSelected) AppColors.Purple.copy(alpha = 0.08f) else (if (isImage) Color.Transparent else AppColors.White)
        ),
        border = BorderStroke(
            width = if (isSelectionMode && isSelected) 1.5.dp else 1.dp,
            color = if (isSelectionMode && isSelected) AppColors.Purple else AppColors.Purple.copy(alpha = 0.2f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isImage) {
                AsyncImage(
                    model = file.uri ?: R.drawable.file,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.file)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val typeStr = when {
                            file.mimeType.startsWith("image") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".png") -> "IMG"
                            else -> "FILE"
                        }
                        Text(text = typeStr, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            } else {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    val iconRes = when {
                        file.mimeType == "application/pdf" || nameLower.endsWith(".pdf") -> R.drawable.file
                        file.mimeType.contains("word") || file.mimeType.contains("officedocument.wordprocessingml") || nameLower.endsWith(".doc") || nameLower.endsWith(".docx") -> R.drawable.doc
                        FileType.fromMimeType(file.mimeType) == FileType.PPT || nameLower.endsWith(".ppt") || nameLower.endsWith(".pptx") -> R.drawable.pptx
                        else -> R.drawable.file
                    }
                    Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 40.dp))
                    val typeStr = when {
                        file.mimeType == "application/pdf" || nameLower.endsWith(".pdf") -> "PDF"
                        file.mimeType.contains("word") || file.mimeType.contains("officedocument.wordprocessingml") || nameLower.endsWith(".doc") || nameLower.endsWith(".docx") -> "DOC"
                        FileType.fromMimeType(file.mimeType) == FileType.PPT || nameLower.endsWith(".ppt") || nameLower.endsWith(".pptx") -> "PPT"
                        else -> "FILE"
                    }
                    Text(text = typeStr, fontSize = 10.sp, color = AppColors.TextSecondary)
                }
            }

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AppColors.Purple else AppColors.White.copy(alpha = 0.8f))
                        .border(1.5.dp, if (isSelected) AppColors.Purple else AppColors.TextSecondary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = AppColors.White, modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tint = if (isImage) AppColors.White else AppColors.TextSecondary
                    IconButton(onClick = onDownload, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = "Download", tint = tint, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onOptions, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                    }
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