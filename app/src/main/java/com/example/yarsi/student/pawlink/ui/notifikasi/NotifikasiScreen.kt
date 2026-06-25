package com.example.yarsi.student.pawlink.ui.notifikasi

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yarsi.student.pawlink.data.repository.NotifikasiModel
import com.example.yarsi.student.pawlink.data.repository.TipeNotifikasi
import com.example.yarsi.student.pawlink.ui.theme.*
import com.example.yarsi.student.pawlink.viewmodel.NotifikasiUiState
import com.example.yarsi.student.pawlink.viewmodel.NotifikasiViewModel


// ── Warna & ikon per tipe ─────────────────────────────────────────────────────

data class NotifStyle(
    val ikonBg: Color,
    val ikonColor: Color,
    val badgeBg: Color,
    val badgeColor: Color,
    val badgeText: String,
    val emoji: String
)

fun styleForTipe(tipe: TipeNotifikasi): NotifStyle = when (tipe) {
    TipeNotifikasi.ADOPSI_DITERIMA -> NotifStyle(
        ikonBg = PawPrimaryLight, ikonColor = PawPrimary,
        badgeBg = PawPrimaryLight, badgeColor = PawPrimary,
        badgeText = "Adopsi", emoji = "✅"
    )
    TipeNotifikasi.ADOPSI_DITOLAK -> NotifStyle(
        ikonBg = Color(0xFFFFE5E5), ikonColor = Color(0xFFD32F2F),
        badgeBg = Color(0xFFFFE5E5), badgeColor = Color(0xFFD32F2F),
        badgeText = "Ditolak", emoji = "❌"
    )
    TipeNotifikasi.HEWAN_DITEMUKAN -> NotifStyle(
        ikonBg = PawBlueLight, ikonColor = PawBlue,
        badgeBg = PawBlueLight, badgeColor = PawBlue,
        badgeText = "Ditemukan", emoji = "📍"
    )
    TipeNotifikasi.POSTINGAN_BARU -> NotifStyle(
        ikonBg = PawAmberLight, ikonColor = PawAmber,
        badgeBg = PawAmberLight, badgeColor = PawAmber,
        badgeText = "Baru", emoji = "🆕"
    )
}


// ── Format waktu ──────────────────────────────────────────────────────────────

fun formatWaktu(waktu: String): String {
    return try {
        val parts = waktu.split("T")[0].split("-")
        val bulan = listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        "${parts[2]} ${bulan[parts[1].toInt()]} ${parts[0]}"
    } catch (e: Exception) { waktu }
}


// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun NotifikasiScreen(
    userId: String = "",
    notifikasiViewModel: NotifikasiViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by notifikasiViewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            notifikasiViewModel.loadNotifikasi(userId)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NotifikasiHeader(
                onBack = onBack,
                jumlahBelumDibaca = uiState.belumDibaca.size,
                onTandaiSemuaDibaca = { notifikasiViewModel.tandaiSemuaDibaca() },
                onHapusSemua = { notifikasiViewModel.hapusSemua() }
            )

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PawPrimary)
                    }
                }
                uiState.errorMessage != null -> {
                    NotifikasiError(pesan = uiState.errorMessage!!)
                }
                uiState.belumDibaca.isEmpty() && uiState.sudahDibaca.isEmpty() -> {
                    NotifikasiKosong()
                }
                else -> {
                    NotifikasiList(
                        uiState = uiState,
                        onTandaiDibaca = { notifikasiViewModel.tandaiDibaca(it) },
                        onHapus = { notifikasiViewModel.hapusNotifikasi(it) }
                    )
                }
            }
        }
    }
}


// ── Header ────────────────────────────────────────────────────────────────────

@Composable
fun NotifikasiHeader(
    onBack: () -> Unit,
    jumlahBelumDibaca: Int,
    onTandaiSemuaDibaca: () -> Unit,
    onHapusSemua: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Brush.verticalGradient(listOf(PawPrimaryDark, PawPrimary)))
    ) {
        Box(
            modifier = Modifier.size(160.dp).offset(x = (-40).dp, y = (-40).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier.size(100.dp).align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
        )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol back
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Logo mini
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Pets, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Notifikasi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (jumlahBelumDibaca > 0) {
                    Text(
                        "$jumlahBelumDibaca belum dibaca",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Menu titik tiga
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tandai semua dibaca") },
                        leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = PawPrimary) },
                        onClick = {
                            onTandaiSemuaDibaca()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus semua", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onHapusSemua()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}


// ── List notifikasi ───────────────────────────────────────────────────────────

@Composable
fun NotifikasiList(
    uiState: NotifikasiUiState,
    onTandaiDibaca: (String) -> Unit,
    onHapus: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Section: Belum Dibaca
        if (uiState.belumDibaca.isNotEmpty()) {
            item {
                NotifikasiSectionHeader(judul = "Belum Dibaca", jumlah = uiState.belumDibaca.size)
            }
            items(uiState.belumDibaca, key = { it.id }) { notif ->
                NotifikasiItemSwipeable(
                    notif = notif,
                    onClick = { onTandaiDibaca(notif.id) },
                    onHapus = { onHapus(notif.id) }
                )
            }
        }

        // Section: Sudah Dibaca
        if (uiState.sudahDibaca.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                NotifikasiSectionHeader(judul = "Sudah Dibaca", jumlah = uiState.sudahDibaca.size)
            }
            items(uiState.sudahDibaca, key = { it.id }) { notif ->
                NotifikasiItemSwipeable(
                    notif = notif,
                    onClick = {},
                    onHapus = { onHapus(notif.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}


// ── Section header ────────────────────────────────────────────────────────────

@Composable
fun NotifikasiSectionHeader(judul: String, jumlah: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(judul, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Box(
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(PawPrimaryLight)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text("$jumlah", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PawPrimary)
        }
    }
}


// ── Item dengan swipe hapus ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiItemSwipeable(
    notif: NotifikasiModel,
    onClick: () -> Unit,
    onHapus: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onHapus()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    Color(0xFFD32F2F) else Color.Transparent,
                label = "swipe_color"
            )
            val scale by animateFloatAsState(
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 1f else 0.8f,
                label = "scale"
            )
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp).scale(scale)
                )
            }
        }
    ) {
        NotifikasiItem(notif = notif, onClick = onClick)
    }
}


// ── Item notifikasi ───────────────────────────────────────────────────────────

@Composable
fun NotifikasiItem(notif: NotifikasiModel, onClick: () -> Unit) {
    val style = styleForTipe(notif.tipe)
    val bgColor = if (notif.sudahDibaca) MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (notif.sudahDibaca) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ikon dengan dot belum dibaca
            Box {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(style.ikonBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(style.emoji, fontSize = 20.sp)
                }
                // Dot hijau kalau belum dibaca
                if (!notif.sudahDibaca) {
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape)
                            .background(PawPrimary)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        notif.judul,
                        fontSize = 13.sp,
                        fontWeight = if (notif.sudahDibaca) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(style.badgeBg).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(style.badgeText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = style.badgeColor)
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    "🐾 ${notif.animalName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PawPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    notif.pesan,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    formatWaktu(notif.waktu),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// ── Empty & Error state ───────────────────────────────────────────────────────

@Composable
fun NotifikasiKosong() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(PawPrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = PawPrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum ada notifikasi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Notifikasi akan muncul saat ada pengajuan adopsi, hewan kamu ditemukan, atau ada postingan baru.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, lineHeight = 19.sp
        )
    }
}

@Composable
fun NotifikasiError(pesan: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(pesan, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}


// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotifikasiScreenPreview() {
    PawLinkTheme {
        NotifikasiScreen()
    }
}