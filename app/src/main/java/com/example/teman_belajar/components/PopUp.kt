package com.example.teman_belajar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.teman_belajar.theme.AppColors

/**
 * Model data untuk item menu di dalam [ActionSelectionDialog].
 *
 * @property title Judul utama item menu (misal: "Ganti Nama Folder").
 * @property subtitle Deskripsi singkat di bawah judul (misal: "Ubah nama folder ini").
 * @property icon Icon yang ditampilkan di sebelah kiri teks.
 * @property onClick Aksi yang dijalankan saat item ini diklik.
 * @property iconTint Warna icon, default menggunakan warna ungu aplikasi.
 * @property iconBgColor Warna latar belakang lingkaran icon.
 * @property titleColor Warna teks judul, default hitam.
 */
data class ActionMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val iconTint: Color = AppColors.Purple,
    val iconBgColor: Color = Color(0xFFF3F0EF),
    val titleColor: Color = Color.Black
)

/**
 * Popup Menu untuk memilih berbagai tindakan (Selection List).
 * Biasanya muncul saat tombol opsi (titik tiga) atau tombol tambah (+) diklik.
 *
 * @param onDismiss Fungsi untuk menutup dialog.
 * @param title Judul kategori di bagian atas (opsional, misal: "TINDAKAN FOLDER").
 * @param items Daftar [ActionMenuItem] yang akan ditampilkan sebagai opsi.
 * @param closeButtonText Teks untuk tombol tutup di bagian paling bawah.
 */
@Composable
fun ActionSelectionDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    items: List<ActionMenuItem>,
    closeButtonText: String = "Tutup"
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
            Column(modifier = Modifier.fillMaxWidth()) {
                if (title != null) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
                    )
                }

                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(item.iconBgColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = item.titleColor
                            )
                            Text(
                                text = item.subtitle,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
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
                        text = closeButtonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Purple
                    )
                }
            }
        }
    }
}

/**
 * Dialog standar untuk input teks (Input Field).
 * Digunakan untuk membuat folder baru atau mengganti nama folder.
 *
 * @param title Judul dialog (misal: "Buat Folder").
 * @param subtitle Instruksi atau keterangan tambahan di bawah judul.
 * @param value State teks yang sedang diketik.
 * @param onValueChange Callback saat teks berubah.
 * @param placeholder Teks petunjuk di dalam input field.
 * @param onDismiss Fungsi saat tombol batal/area luar diklik.
 * @param onConfirm Fungsi saat tombol konfirmasi diklik.
 * @param confirmButtonText Teks untuk tombol konfirmasi.
 * @param dismissButtonText Teks untuk tombol batal.
 */
@Composable
fun TextInputDialog(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonText: String = "Simpan",
    dismissButtonText: String = "Batal"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp) 
        },
        text = {
            Column {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Purple,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                shape = RoundedCornerShape(12.dp),
                enabled = value.isNotBlank()
            ) {
                Text(confirmButtonText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText, color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

/**
 * Dialog Konfirmasi untuk tindakan kritis (misal: Hapus).
 * Menampilkan ikon peringatan besar dan pesan konfirmasi yang tegas.
 *
 * @param title Judul besar (misal: "Hapus Folder").
 * @param description Pesan peringatan detail.
 * @param icon Icon pusat (misal: Icons.Default.Delete).
 * @param iconTint Warna ikon peringatan (default Merah).
 * @param iconBgColor Warna latar belakang lingkaran icon.
 * @param confirmButtonText Teks tombol eksekusi (misal: "Iya, Hapus").
 * @param confirmButtonColor Warna tombol eksekusi (default Merah).
 * @param dismissButtonText Teks untuk tombol batal.
 * @param onDismiss Fungsi untuk membatalkan tindakan.
 * @param onConfirm Fungsi untuk mengonfirmasi tindakan.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color = Color(0xFFDC2626),
    iconBgColor: Color = Color(0xFFFEF2F2),
    confirmButtonText: String,
    confirmButtonColor: Color = Color(0xFFB91C1C),
    dismissButtonText: String,
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
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tombol Konfirmasi (Tindakan Utama)
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(confirmButtonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Batalkan (Tindakan Sekunder)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(dismissButtonText, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
