package com.example.yarsi.student.pawlink.ui.profil

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yarsi.student.pawlink.ui.theme.*

// ─── Data State ───────────────────────────────────────────────────────────────
data class ProfilUiState(
    val nama: String = "",
    val email: String = "",
    val noHp: String = "",
    val kota: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val showLogoutDialog: Boolean = false
)

// ─── Main Screen ──────────────────────────────────────────────────────────────
@Composable
fun ProfilScreen(
    nama: String = "Alfaridzi Dieza",
    email: String = "dieza@email.com",
    noHp: String = "08123456789",
    kota: String = "Jakarta Selatan",
    role: String = "pelapor",
    photoUrl: String = "",
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSaveProfile: (nama: String, noHp: String, kota: String) -> Unit = { _, _, _ -> }
) {
    var state by remember {
        mutableStateOf(
            ProfilUiState(
                nama = nama,
                email = email,
                noHp = noHp,
                kota = kota,
                role = role,
                photoUrl = photoUrl
            )
        )
    }

    // Edit state sementara — hanya disimpan saat klik Simpan
    var editNama by remember { mutableStateOf(nama) }
    var editNoHp by remember { mutableStateOf(noHp) }
    var editKota by remember { mutableStateOf(kota) }
    var editPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { editPhotoUri = it }
    }

    // Logout dialog
    if (state.showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                state = state.copy(showLogoutDialog = false)
                onLogout()
            },
            onDismiss = {
                state = state.copy(showLogoutDialog = false)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProfilHeader(
            isEditMode = state.isEditMode,
            onBack = onBack,
            onEditToggle = {
                if (state.isEditMode) {
                    // Reset edit state saat batal
                    editNama = state.nama
                    editNoHp = state.noHp
                    editKota = state.kota
                    editPhotoUri = null
                }
                state = state.copy(isEditMode = !state.isEditMode)
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto profil
            FotoProfilSection(
                nama = if (state.isEditMode) editNama else state.nama,
                photoUrl = state.photoUrl,
                editPhotoUri = editPhotoUri,
                isEditMode = state.isEditMode,
                role = state.role,
                onPickPhoto = { photoPicker.launch("image/*") }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Info profil
            if (state.isEditMode) {
                EditProfilSection(
                    nama = editNama,
                    noHp = editNoHp,
                    kota = editKota,
                    email = state.email,
                    onNamaChange = { editNama = it },
                    onNoHpChange = { editNoHp = it },
                    onKotaChange = { editKota = it }
                )
            } else {
                LihatProfilSection(
                    nama = state.nama,
                    email = state.email,
                    noHp = state.noHp,
                    kota = state.kota,
                    role = state.role
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol simpan (hanya di edit mode)
            if (state.isEditMode) {
                Button(
                    onClick = {
                        state = state.copy(
                            nama = editNama,
                            noHp = editNoHp,
                            kota = editKota,
                            isEditMode = false
                        )
                        onSaveProfile(editNama, editNoHp, editKota)
                    },
                    enabled = editNama.isNotBlank() && editNoHp.isNotBlank() && editKota.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Perubahan",
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Tombol logout
            OutlinedButton(
                onClick = { state = state.copy(showLogoutDialog = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.error
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
fun ProfilHeader(
    isEditMode: Boolean,
    onBack: () -> Unit,
    onEditToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isEditMode) "Edit Profil" else "Profil Saya",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    if (isEditMode) "Ubah informasi profilmu" else "Informasi akun PawLink",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }

            // Tombol edit / batal
            TextButton(onClick = onEditToggle) {
                Text(
                    if (isEditMode) "Batal" else "Edit",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── Foto Profil Section ──────────────────────────────────────────────────────
@Composable
fun FotoProfilSection(
    nama: String,
    photoUrl: String,
    editPhotoUri: Uri?,
    isEditMode: Boolean,
    role: String,
    onPickPhoto: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .then(
                    if (isEditMode) Modifier.clickable { onPickPhoto() }
                    else Modifier
                )
        ) {
            // Foto profil
            if (editPhotoUri != null) {
                AsyncImage(
                    model = editPhotoUri,
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nama.take(2).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Ikon kamera saat edit mode
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            nama.ifBlank { "Pengguna PawLink" },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Badge role
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (role == "pelapor") PawPrimaryLight else PawBlueLight
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                role.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (role == "pelapor") PawPrimary else PawBlue
            )
        }
    }
}

// ─── Lihat Profil (Read only) ─────────────────────────────────────────────────
@Composable
fun LihatProfilSection(
    nama: String,
    email: String,
    noHp: String,
    kota: String,
    role: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Informasi Akun", fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(4.dp))

        ProfilInfoItem(
            icon = Icons.Outlined.Person,
            label = "Nama Lengkap",
            value = nama.ifBlank { "-" }
        )
        ProfilInfoItem(
            icon = Icons.Outlined.Email,
            label = "Email",
            value = email.ifBlank { "-" }
        )
        ProfilInfoItem(
            icon = Icons.Outlined.Phone,
            label = "Nomor HP",
            value = noHp.ifBlank { "-" }
        )
        ProfilInfoItem(
            icon = Icons.Outlined.LocationOn,
            label = "Kota",
            value = kota.ifBlank { "-" }
        )
        ProfilInfoItem(
            icon = Icons.Outlined.Badge,
            label = "Role",
            value = role.replaceFirstChar { it.uppercase() }.ifBlank { "-" }
        )
    }
}

@Composable
fun ProfilInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// ─── Edit Profil Section ──────────────────────────────────────────────────────
@Composable
fun EditProfilSection(
    nama: String,
    noHp: String,
    kota: String,
    email: String,
    onNamaChange: (String) -> Unit,
    onNoHpChange: (String) -> Unit,
    onKotaChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Edit Informasi", fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)

        ProfilTextField(
            value = nama,
            onValueChange = onNamaChange,
            label = "Nama Lengkap",
            placeholder = "Masukkan nama lengkap",
            icon = Icons.Outlined.Person
        )

        // Email tidak bisa diedit
        Column {
            Text("Email", fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp))
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text("Email tidak dapat diubah",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        ProfilTextField(
            value = noHp,
            onValueChange = onNoHpChange,
            label = "Nomor HP",
            placeholder = "08xxxxxxxxxx",
            icon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone
        )

        ProfilTextField(
            value = kota,
            onValueChange = onKotaChange,
            label = "Kota",
            placeholder = "Nama kota kamu",
            icon = Icons.Outlined.LocationOn
        )
    }
}

// ─── Reusable TextField ───────────────────────────────────────────────────────
@Composable
fun ProfilTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingIcon = {
                Icon(icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

// ─── Logout Dialog ────────────────────────────────────────────────────────────
@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text("Keluar dari Akun?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
        },
        text = {
            Text("Kamu akan keluar dari akun PawLink. Pastikan kamu ingat email dan password untuk masuk kembali.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ya, Keluar", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────
@Preview(name = "Profil - Lihat", showBackground = true, showSystemUi = true)
@Composable
fun ProfilLihatPreview() {
    PawLinkTheme {
        ProfilScreen(
            nama = "Alfaridzi Dieza",
            email = "dieza@email.com",
            noHp = "08123456789",
            kota = "Jakarta Selatan",
            role = "pelapor"
        )
    }
}

@Preview(name = "Profil - Pencari", showBackground = true, showSystemUi = true)
@Composable
fun ProfilPencariPreview() {
    PawLinkTheme {
        ProfilScreen(
            nama = "Budi Santoso",
            email = "budi@email.com",
            noHp = "08987654321",
            kota = "Bandung",
            role = "pencari"
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfilDarkPreview() {
    PawLinkTheme(darkTheme = true) {
        ProfilScreen(
            nama = "Alfaridzi Dieza",
            email = "dieza@email.com",
            noHp = "08123456789",
            kota = "Jakarta Selatan",
            role = "pelapor"
        )
    }
}