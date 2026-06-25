package com.example.yarsi.student.pawlink.ui.posting

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
import com.example.yarsi.student.pawlink.viewmodel.HewanViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yarsi.student.pawlink.data.repository.HewanModel
import com.example.yarsi.student.pawlink.utils.LocationHelper
import com.example.yarsi.student.pawlink.utils.rememberLocationPermissionState
import kotlinx.coroutines.launch

// Tipe Posting
enum class TipePosting(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
) {
    ADOPSI(
        label = "Adopsi",
        description = "Tawarkan hewan untuk diadopsi",
        icon = Icons.Outlined.Pets,
        color = PawPrimary,
        bgColor = PawPrimaryLight
    ),
    HILANG(
        label = "Hilang",
        description = "Laporkan hewan yang hilang",
        icon = Icons.Outlined.Search,
        color = PawAmber,
        bgColor = PawAmberLight
    ),
    DITEMUKAN(
        label = "Ditemukan",
        description = "Lapor hewan yang kamu temukan",
        icon = Icons.Outlined.LocationOn,
        color = PawBlue,
        bgColor = PawBlueLight
    )
}

// Form State
data class PostingFormState(
    val tipePosting: TipePosting? = null,
    val namaHewan: String = "",
    val jenisHewan: String = "",
    val ras: String = "",
    val usia: String = "",
    val jenisKelamin: String = "",
    val deskripsi: String = "",
    val photoUri: Uri? = null,
    val lokasi: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDetectingLocation: Boolean = false
)

// Main Screen
@Composable
fun PostingHewanScreen(
    userRole: String = "pelapor", // "pelapor" atau "pencari"
    userId: String = "",
    userCity: String = "",
    onBack: () -> Unit = {},
    onPostingSuccess: () -> Unit = {},
    hewanViewModel: HewanViewModel = viewModel()
) {
    val context = LocalContext.current
    var formState by remember { mutableStateOf(PostingFormState()) }
    var currentStep by remember { mutableIntStateOf(1) } // 1: pilih tipe, 2: isi form

    // Filter tipe posting berdasarkan role
    val availableTipes = remember(userRole) {
        if (userRole.lowercase() == "pencari") {
            listOf(TipePosting.DITEMUKAN) // pencari hanya bisa lapor ditemukan
        } else {
            listOf(TipePosting.ADOPSI, TipePosting.HILANG, TipePosting.DITEMUKAN)
        }
    }

    val uiState by hewanViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isPostingSuccess) {
        if (uiState.isPostingSuccess) {
            hewanViewModel.resetPostingState()
            onPostingSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PostingHeader(
            currentStep = currentStep,
            tipePosting = formState.tipePosting,
            onBack = {
                if (currentStep > 1) currentStep = 1
                else onBack()
            }
        )

        if (currentStep == 1) {
            PilihTipePosting(
                availableTipes = availableTipes,
                userRole = userRole,
                selectedTipe = formState.tipePosting,
                onTipeSelected = { formState = formState.copy(tipePosting = it) },
                onNext = { if (formState.tipePosting != null) currentStep = 2 }
            )
        } else {
            IsiFormPosting(
                formState = formState,
                userCity = userCity,
                onFormChange = { formState = it },
                onSubmit = {
                    android.util.Log.d("PawLink", "Tombol Publish ditekan")
                    val photoUri = formState.photoUri ?: return@IsiFormPosting

                    val helperNamaHewan =
                        if (
                            formState.tipePosting == TipePosting.DITEMUKAN &&
                            formState.namaHewan.isBlank()
                        ) {
                            "Nama tidak diketahui"
                        } else {
                            formState.namaHewan
                        }

                    val hewan = HewanModel(
                        userId = userId,
                        name = helperNamaHewan,
                        type = formState.jenisHewan,
                        breed = formState.ras,
                        age = formState.usia,
                        gender = formState.jenisKelamin,
                        description = formState.deskripsi,
                        location    = formState.lokasi,

                        status = when (formState.tipePosting) {
                            TipePosting.ADOPSI -> "tersedia"
                            TipePosting.HILANG -> "hilang"
                            TipePosting.DITEMUKAN -> "ditemukan"
                            else -> "tersedia"
                        },

                        postType = when (formState.tipePosting) {
                            TipePosting.ADOPSI -> "adopsi"
                            TipePosting.HILANG -> "hilang"
                            TipePosting.DITEMUKAN -> "ditemukan"
                            else -> ""
                        },

                        latitude = formState.latitude,
                        longitude = formState.longitude
                    )

                    hewanViewModel?.publishHewan(
                        context = context,
                        hewan = hewan,
                        imageUri = photoUri
                    )
                }
            )
        }
    }
}

// Header
@Composable
fun PostingHeader(
    currentStep: Int,
    tipePosting: TipePosting?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary)
                )
            )
    ) {
        // Dekorasi lingkaran
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
                .padding(top = 48.dp, start = 16.dp, end = 24.dp),
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
            Column {
                Text(
                    if (currentStep == 1) "Buat Postingan"
                    else "Detail ${tipePosting?.label ?: "Hewan"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    if (currentStep == 1) "Pilih jenis postingan"
                    else "Lengkapi informasi hewan",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Step 1: Pilih Tipe Posting
@Composable
fun PilihTipePosting(
    availableTipes: List<TipePosting>,
    userRole: String,
    selectedTipe: TipePosting?,
    onTipeSelected: (TipePosting) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Apa yang ingin kamu posting?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Info role pencari
        if (userRole == "pencari") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Text(
                    "Sebagai Pencari, kamu bisa melaporkan hewan yang kamu temukan di sekitarmu.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 19.sp
                )
            }
        }

        // Kartu tipe posting
        availableTipes.forEach { tipe ->
            TipePostingCard(
                tipe = tipe,
                isSelected = selectedTipe == tipe,
                onClick = { onTipeSelected(tipe) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNext,
            enabled = selectedTipe != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text("Lanjutkan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun TipePostingCard(
    tipe: TipePosting,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) tipe.color else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) tipe.bgColor
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) tipe.color else MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Icon(tipe.icon, contentDescription = null,
                    tint = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(tipe.label, fontWeight = FontWeight.Bold,
                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(tipe.description, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null,
                    tint = tipe.color, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// Step 2: Isi Form Posting
@Composable
fun IsiFormPosting(
    formState: PostingFormState,
    userCity: String = "",
    onFormChange: (PostingFormState) -> Unit,
    onSubmit: () -> Unit
) {
    val tipe = formState.tipePosting ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationPermission = rememberLocationPermissionState(
        onGranted = {
            coroutineScope.launch {
                onFormChange(formState.copy(isDetectingLocation = true))
                val result = LocationHelper.getCurrentLocation(context)
                result.onSuccess { lokasi ->
                    onFormChange(formState.copy(
                        lokasi = lokasi.alamat,
                        latitude = lokasi.latitude,
                        longitude = lokasi.longitude,
                        isDetectingLocation = false
                    ))
                }.onFailure {
                    onFormChange(formState.copy(isDetectingLocation = false))
                }
            }
        },
        onDenied = {
            onFormChange(formState.copy(isDetectingLocation = false))
        }
    )
    val isFormValid = formState.photoUri != null &&
            formState.jenisHewan.isNotBlank() &&
            formState.deskripsi.isNotBlank() &&
            formState.lokasi.isNotBlank()

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFormChange(formState.copy(photoUri = it)) }
    }

    // Tambah di dalam IsiFormPosting, sebelum Column
    LaunchedEffect(Unit) {
        if (formState.lokasi.isBlank()) {
            onFormChange(formState.copy(
                lokasi = userCity.ifBlank { "Lokasi tidak diketahui" }
            ))
        }

        if (LocationHelper.isLocationPermissionGranted(context)) {
            onFormChange(formState.copy(isDetectingLocation = true))
            val result = LocationHelper.getCurrentLocation(context)
            result.onSuccess { lokasi ->
                onFormChange(formState.copy(
                    lokasi = lokasi.alamat,
                    latitude = lokasi.latitude,
                    longitude = lokasi.longitude,
                    isDetectingLocation = false
                ))
            }.onFailure {
                onFormChange(formState.copy(isDetectingLocation = false))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Badge tipe
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(tipe.bgColor)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(tipe.icon, contentDescription = null,
                tint = tipe.color, modifier = Modifier.size(14.dp))
            Text(tipe.label, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = tipe.color)
        }

        // Upload foto
        FotoUploadSection(
            photoUri = formState.photoUri,
            tipe = tipe,
            onPickPhoto = { photoPicker.launch("image/*") }
        )

        // Form fields
        if (tipe != TipePosting.DITEMUKAN) {
            PostingTextField(
                value = formState.namaHewan,
                onValueChange = { onFormChange(formState.copy(namaHewan = it)) },
                label = "Nama Hewan",
                placeholder = "Contoh: Mochi, Brownie",
                icon = Icons.Outlined.Pets
            )
        }

        PostingTextField(
            value = formState.jenisHewan,
            onValueChange = { onFormChange(formState.copy(jenisHewan = it)) },
            label = "Jenis Hewan *",
            placeholder = "Contoh: Kucing, Anjing, Kelinci",
            icon = Icons.Outlined.Category
        )

        PostingTextField(
            value = formState.ras,
            onValueChange = { onFormChange(formState.copy(ras = it)) },
            label = "Ras",
            placeholder = "Contoh: Persia, Golden Retriever",
            icon = Icons.Outlined.Pets
        )

        // Usia & Jenis Kelamin dalam satu row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PostingTextField(
                value = formState.usia,
                onValueChange = { onFormChange(formState.copy(usia = it)) },
                label = "Usia",
                placeholder = "Contoh: 2 tahun",
                icon = Icons.Outlined.Cake,
                modifier = Modifier.weight(1f)
            )
            JenisKelaminDropdown(
                selected = formState.jenisKelamin,
                onSelected = { onFormChange(formState.copy(jenisKelamin = it)) },
                modifier = Modifier.weight(1f)
            )
        }

        PostingTextField(
            value = formState.deskripsi,
            onValueChange = { onFormChange(formState.copy(deskripsi = it)) },
            label = "Deskripsi *",
            placeholder = if (tipe == TipePosting.DITEMUKAN)
                "Ceritakan di mana dan kapan kamu menemukannya..."
            else "Ceritakan tentang hewan ini...",
            icon = Icons.Outlined.Description,
            maxLines = 4,
            singleLine = false
        )

        // Lokasi otomatis
        LokasiSection(
            lokasi = formState.lokasi,
            isDetecting = formState.isDetectingLocation,
            userCity = userCity,
            onDetectLocation = {
                if (LocationHelper.isLocationPermissionGranted(context)) {
                    coroutineScope.launch {
                        onFormChange(formState.copy(isDetectingLocation = true))
                        val result = LocationHelper.getCurrentLocation(context)
                        result.onSuccess { lokasi ->
                            onFormChange(formState.copy(
                                lokasi = lokasi.alamat,
                                latitude = lokasi.latitude,
                                longitude = lokasi.longitude,
                                isDetectingLocation = false
                            ))
                        }.onFailure {
                            onFormChange(formState.copy(isDetectingLocation = false))
                        }
                    }
                } else {
                    locationPermission.requestPermission()
                }
            }
        )

        // Wajib diisi info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp))
            Text("Field bertanda * wajib diisi",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSubmit,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tipe.color,
                disabledContainerColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publikasikan ke PawLink",
                fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Foto Upload Section
@Composable
fun FotoUploadSection(
    photoUri: Uri?,
    tipe: TipePosting,
    onPickPhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Foto Hewan *", fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (photoUri == null) 1.5.dp else 0.dp,
                    color = if (photoUri == null) MaterialTheme.colorScheme.outline
                    else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    if (photoUri == null) MaterialTheme.colorScheme.surface
                    else Color.Transparent
                )
                .clickable { onPickPhoto() },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto hewan",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay tombol ganti foto
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(tipe.color)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(tipe.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null,
                            tint = tipe.color, modifier = Modifier.size(28.dp))
                    }
                    Text("Upload Foto Hewan",
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("Ketuk untuk pilih dari galeri",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// Lokasi Section
@Composable
fun LokasiSection(
    lokasi: String,
    userCity: String,
    isDetecting: Boolean,
    onDetectLocation: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Lokasi *", fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))

            Text(
                if (lokasi.isBlank()) "Lokasi belum terdeteksi"
                else lokasi,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = if (lokasi.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )

            if (isDetecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                TextButton(
                    onClick = onDetectLocation,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (lokasi.isBlank()) "Deteksi" else "Perbarui",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Jenis Kelamin Dropdown
@Composable
fun JenisKelaminDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Jantan", "Betina", "Tidak diketahui")

    Column(modifier = modifier) {
        Text("Jenis Kelamin", fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    selected.ifBlank { "Pilih" },
                    color = if (selected.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Reusable TextField
@Composable
fun PostingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp))
            },
            singleLine = singleLine,
            maxLines = maxLines,
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

// Previews
@Preview(name = "Pilih Tipe - Pelapor", showBackground = true, showSystemUi = true)
@Composable
fun PostingPelaporPreview() {
    PawLinkTheme {
        PostingHewanScreen(userRole = "pelapor")
    }
}

@Preview(name = "Pilih Tipe - Pencari", showBackground = true, showSystemUi = true)
@Composable
fun PostingPencariPreview() {
    PawLinkTheme {
        PostingHewanScreen(userRole = "pencari")
    }
}

@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PostingDarkPreview() {
    PawLinkTheme(darkTheme = true) {
        PostingHewanScreen(userRole = "pelapor")
    }
}
@Preview(name = "Form Adopsi", showBackground = true, showSystemUi = true)
@Composable
fun FormAdopsiPreview() {
    PawLinkTheme {
        IsiFormPosting(
            formState = PostingFormState(tipePosting = TipePosting.ADOPSI),
            onFormChange = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Form Hilang", showBackground = true, showSystemUi = true)
@Composable
fun FormHilangPreview() {
    PawLinkTheme {
        IsiFormPosting(
            formState = PostingFormState(tipePosting = TipePosting.HILANG),
            onFormChange = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Form Ditemukan", showBackground = true, showSystemUi = true)
@Composable
fun FormDitemukanPreview() {
    PawLinkTheme {
        IsiFormPosting(
            formState = PostingFormState(tipePosting = TipePosting.DITEMUKAN),
            onFormChange = {},
            onSubmit = {}
        )
    }
}