package com.example.yarsi.student.pawlink.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState as remHScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yarsi.student.pawlink.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yarsi.student.pawlink.data.repository.HewanModel
import com.example.yarsi.student.pawlink.ui.theme.PawAmber
import com.example.yarsi.student.pawlink.ui.theme.PawAmberLight
import com.example.yarsi.student.pawlink.ui.theme.PawBlue
import com.example.yarsi.student.pawlink.ui.theme.PawBlueLight
import com.example.yarsi.student.pawlink.ui.theme.PawLinkTheme
import com.example.yarsi.student.pawlink.ui.theme.PawPrimary
import com.example.yarsi.student.pawlink.ui.theme.PawPrimaryDark
import com.example.yarsi.student.pawlink.ui.theme.PawPrimaryLight
import com.example.yarsi.student.pawlink.viewmodel.HewanViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.yarsi.student.pawlink.utils.LocationHelper
import com.example.yarsi.student.pawlink.utils.rememberLocationPermissionState
import kotlinx.coroutines.launch


// Data models

enum class HewanStatus(val label: String, val color: Color, val bgColor: Color) {
    TERSEDIA("Tersedia", PawPrimary, PawPrimaryLight),
    HILANG("Hilang", PawAmber, PawAmberLight),
    DITEMUKAN("Ditemukan", PawBlue, PawBlueLight),
    TERADOPSI("Teradopsi", Color(0xFF888780), Color(0xFFF1EFE8))
}

data class HewanItem(
    val id: String,
    val nama: String,
    val jenis: String,
    val usia: String,
    val lokasi: String,
    val status: HewanStatus,
    val inisialPelapor: String,
    val warnaPelapor: Color = PawPrimary
)

data class TimelineItem(
    val inisial: String,
    val warna: Color,
    val nama: String,
    val aksi: String,
    val detail: String,
    val waktu: String
)

enum class FilterChip(val label: String) {
    SEMUA("Semua"),
    ADOPSI("Adopsi"),
    HILANG("Hilang"),
    KUCING("Kucing"),
    ANJING("Anjing")
}

enum class NavItem(val label: String, val icon: ImageVector) {
    BERANDA("Beranda", Icons.Outlined.Home),
    PETA("Peta", Icons.Outlined.Map),
    NOTIFIKASI("Notifikasi", Icons.Outlined.Notifications),
    PROFIL("Profil", Icons.Outlined.Person)
}

// Sample data

//val sampleHewan = listOf(
//    HewanItem("1", "Mochi", "Kucing", "1 thn", "Jaksel", HewanStatus.TERSEDIA, "AR"),
//    HewanItem("2", "Brownie", "Anjing", "3 thn", "Jakpus", HewanStatus.HILANG, "BW", Color(0xFF7B4F2A)),
//    HewanItem("3", "Unknown", "Kucing", "?", "Jakbar", HewanStatus.DITEMUKAN, "RN", PawBlue),
//    HewanItem("4", "Luna", "Kucing", "2 thn", "Bekasi", HewanStatus.TERSEDIA, "SN")
//)

//val sampleTimeline = listOf(
//    TimelineItem("AR", PawPrimary, "Anisa", "memposting hewan untuk adopsi", "Mochi · Kucing betina", "2 mnt lalu"),
//    TimelineItem("BW", Color(0xFF7B4F2A), "Budi", "melaporkan hewan hilang", "Brownie · Anjing jantan", "15 mnt lalu"),
//    TimelineItem("RN", PawBlue, "Rita", "menemukan hewan tak dikenal", "Kucing · Jakbar", "1 jam lalu"),
//    TimelineItem("DS", Color(0xFF6B4FA0), "Dian", "mengadopsi hewan", "Biscuit · Kucing jantan", "3 jam lalu")
//)


// Main Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel = viewModel(),
    hewanViewModel: HewanViewModel = viewModel(),
    onHewanClick: (String) -> Unit = {},
    onPostingClick: () -> Unit = {},
    onNotifikasiClick: () -> Unit = {},
    onProfilClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedNav by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf(FilterChip.SEMUA) }
    var searchQuery by remember { mutableStateOf("") }

    val userName by authViewModel.userName.collectAsState()
    val userCity by authViewModel.userCity.collectAsState()
    val userPhotoUrl by authViewModel.userPhotoUrl.collectAsState()
    LaunchedEffect(userPhotoUrl) {
        android.util.Log.d("PawLink", "userPhotoUrl = $userPhotoUrl")
    }

    val uiState by hewanViewModel.uiState.collectAsState()
    val radiusKm by hewanViewModel.radiusKm.collectAsState()

    // Permission handler
    val locationPermission = rememberLocationPermissionState(
        onGranted = {
            coroutineScope.launch {
                val result = LocationHelper.getCurrentLocation(context)
                result.onSuccess { lokasi ->
                    hewanViewModel.setLokasiUser(lokasi.latitude, lokasi.longitude)
                }
            }
        }
    )

    // Load data & ambil lokasi saat pertama masuk
    LaunchedEffect(Unit) {
        hewanViewModel.loadSemuaHewan()
        hewanViewModel.loadAktivitasTerbaru()
        hewanViewModel.subscribeRealtimeHewan()

        if (LocationHelper.isLocationPermissionGranted(context)) {
            val result = LocationHelper.getCurrentLocation(context)
            result.onSuccess { lokasi ->
                hewanViewModel.setLokasiUser(lokasi.latitude, lokasi.longitude)
            }
        } else {
            locationPermission.requestPermission()
        }
    }

    // Hewan terdekat + filter chip + search
    val hewanTerdekat = remember(uiState.hewanList, selectedFilter, searchQuery, radiusKm) {
        hewanViewModel.getHewanTerdekat().filter { hewan ->
            val matchFilter = when (selectedFilter) {
                FilterChip.SEMUA  -> true
                FilterChip.ADOPSI -> hewan.status == "tersedia"
                FilterChip.HILANG -> hewan.status == "hilang"
                FilterChip.KUCING -> hewan.type.equals("kucing", ignoreCase = true)
                FilterChip.ANJING -> hewan.type.equals("anjing", ignoreCase = true)
            }
            val matchSearch = searchQuery.isBlank() ||
                    hewan.name.contains(searchQuery, ignoreCase = true) ||
                    hewan.type.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomNav(
                selectedIndex = selectedNav,
                onItemSelected = { selectedNav = it },
                onPostingClick = onPostingClick,
                onNotifikasiClick = onNotifikasiClick,
                onProfilClick = onProfilClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            DashboardHeader(
                userName = userName,
                userCity = userCity,
                userPhotoUrl = userPhotoUrl,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onNotifikasiClick = onNotifikasiClick
            )

            FilterRow(selectedFilter = selectedFilter, onFilterSelected = { selectedFilter = it })

            StatistikRow(
                tersedia  = uiState.totalTersedia,
                hilang    = uiState.totalHilang,
                ditemukan = uiState.totalDitemukan
            )

            RadiusSlider(radiusKm = radiusKm, onRadiusChange = { hewanViewModel.setRadius(it) })

            SectionHeader(title = "Terdekat dari kamu", onLihatSemua = {})

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PawPrimary)
                }
            } else {
                HewanCardRow(hewanList = hewanTerdekat, onHewanClick = onHewanClick)
            }

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = "Linimasa terbaru", onLihatSemua = {})

            if (uiState.aktivitasList.isEmpty()) {
                Text(
                    "Belum ada aktivitas terbaru",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                uiState.aktivitasList.forEach { hewan ->
                    AktivitasItemRow(hewan = hewan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AktivitasItemRow(hewan: HewanModel) {
    val aksi = when (hewan.status.lowercase()) {
        "tersedia"  -> "memposting hewan untuk adopsi"
        "hilang"    -> "melaporkan hewan hilang"
        "ditemukan" -> "menemukan hewan tak dikenal"
        "teradopsi" -> "mengadopsi hewan"
        else        -> "memposting hewan"
    }

    val warna = when (hewan.status.lowercase()) {
        "tersedia"  -> PawPrimary
        "hilang"    -> PawAmber
        "ditemukan" -> PawBlue
        else        -> PawPrimary
    }

    val inisial = hewan.name.take(2).uppercase().ifBlank { "??" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(warna),
            contentAlignment = Alignment.Center
        ) {
            Text(inisial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                aksi,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${hewan.name.ifBlank { "Tanpa nama" }} · ${hewan.type}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
fun RadiusSlider(radiusKm: Double, onRadiusChange: (Double) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null,
            tint = PawPrimary, modifier = Modifier.size(16.dp))
        Slider(value = radiusKm.toFloat(), onValueChange = { onRadiusChange(it.toDouble()) },
            valueRange = 1f..50f, modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = PawPrimary,
                activeTrackColor = PawPrimary, inactiveTrackColor = PawPrimaryLight))
        Text("${radiusKm.toInt()} km", fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, color = PawPrimary, modifier = Modifier.width(40.dp))
    }
}

@Composable
fun DashboardHeader(
    userName: String,
    userCity: String,
    userPhotoUrl: String = "",
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNotifikasiClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()
        .background(Brush.verticalGradient(listOf(PawPrimaryDark, PawPrimary)))) {
        Box(modifier = Modifier.size(160.dp).offset(x = (-40).dp, y = (-40).dp)
            .clip(CircleShape).background(Color.White.copy(alpha = 0.07f)))
        Box(modifier = Modifier.size(120.dp).align(Alignment.TopEnd)
            .offset(x = 40.dp, y = (-30).dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f)))
        Column(modifier = Modifier.fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null,
                            tint = PawPrimaryLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(userCity.ifBlank { "Lokasi tidak diketahui" }, fontSize = 12.sp, color = PawPrimaryLight)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Halo, $userName 👋", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgedBox(badge = { Badge(containerColor = PawAmber, modifier = Modifier.size(8.dp)) }) {
                        IconButton(onClick = onNotifikasiClick,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifikasi",
                                tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Foto profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                userName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = searchQuery, onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari hewan di sekitar kamu...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) },
                singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White))
        }
    }
}

@Composable
fun FilterRow(selectedFilter: FilterChip, onFilterSelected: (FilterChip) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(remHScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip.entries.forEach { chip ->
            val isSelected = chip == selectedFilter
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) PawPrimary else MaterialTheme.colorScheme.surface)
                .then(if (!isSelected) Modifier.padding(1.dp) else Modifier)
                .clickable { onFilterSelected(chip) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(chip.label, fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StatistikRow(tersedia: Int, hilang: Int, ditemukan: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("$tersedia", "Tersedia", PawPrimary, PawPrimaryLight, showDot = true, modifier = Modifier.weight(1f))
        StatCard("$hilang", "Hilang", PawAmber, PawAmberLight, modifier = Modifier.weight(1f))
        StatCard("$ditemukan", "Ditemukan", PawBlue, PawBlueLight, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(angka: String, label: String, angkaColor: Color, bgColor: Color, showDot: Boolean = false, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(angka, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = angkaColor)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (showDot) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(angkaColor))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(label, fontSize = 11.sp, color = angkaColor.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onLihatSemua: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Lihat semua", fontSize = 13.sp, color = PawPrimary,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onLihatSemua() })
    }
}

@Composable
fun HewanCardRow(hewanList: List<HewanModel>, onHewanClick: (String) -> Unit) {
    if (hewanList.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada hewan di sekitarmu", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(remHScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        hewanList.forEach { hewan -> HewanCard(hewan = hewan, onClick = { onHewanClick(hewan.id) }) }
    }
}

@Composable
fun HewanCard(hewan: HewanModel, onClick: () -> Unit) {
    val status = when (hewan.status.lowercase()) {
        "tersedia" -> HewanStatus.TERSEDIA
        "hilang" -> HewanStatus.HILANG
        "ditemukan" -> HewanStatus.DITEMUKAN
        "teradopsi" -> HewanStatus.TERADOPSI
        else -> HewanStatus.TERSEDIA
    }
    Card(onClick = onClick, modifier = Modifier.width(150.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hewan.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = hewan.photoUrl,
                        contentDescription = "Foto hewan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(
                                listOf(PawPrimary.copy(alpha = 0.7f), PawPrimary)
                            )),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Pets, contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)).background(status.bgColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(status.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = status.color)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(hewan.name.ifBlank { "Tanpa nama" }, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${hewan.type} · ${hewan.age}", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun TimelineItemRow(item: TimelineItem) {
    Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(item.warna), contentAlignment = Alignment.Center) {
            Text(item.inisial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("${item.nama} ${item.aksi}", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text("${item.detail} · ${item.waktu}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
    }
    Box(modifier = Modifier.fillMaxWidth().padding(start = 68.dp).height(0.5.dp)
        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
}

@Composable
fun DashboardBottomNav(
    selectedIndex: Int, onItemSelected: (Int) -> Unit,
    onPostingClick: () -> Unit, onNotifikasiClick: () -> Unit, onProfilClick: () -> Unit
) {
    Box {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            NavigationBarItem(selected = selectedIndex == 0, onClick = { onItemSelected(0) },
                icon = { Icon(NavItem.BERANDA.icon, contentDescription = NavItem.BERANDA.label, modifier = Modifier.size(24.dp)) },
                label = { Text(NavItem.BERANDA.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = PawPrimary, selectedTextColor = PawPrimary, indicatorColor = PawPrimaryLight))
            NavigationBarItem(selected = selectedIndex == 1, onClick = { onItemSelected(1) },
                icon = { Icon(NavItem.PETA.icon, contentDescription = NavItem.PETA.label, modifier = Modifier.size(24.dp)) },
                label = { Text(NavItem.PETA.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = PawPrimary, selectedTextColor = PawPrimary, indicatorColor = PawPrimaryLight))
            NavigationBarItem(selected = false, onClick = {},
                icon = { Spacer(modifier = Modifier.size(24.dp)) }, label = { Text("") }, enabled = false)
            NavigationBarItem(selected = selectedIndex == 2, onClick = { onItemSelected(2); onNotifikasiClick() },
                icon = { Icon(NavItem.NOTIFIKASI.icon, contentDescription = NavItem.NOTIFIKASI.label, modifier = Modifier.size(24.dp)) },
                label = { Text(NavItem.NOTIFIKASI.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = PawPrimary, selectedTextColor = PawPrimary, indicatorColor = PawPrimaryLight))
            NavigationBarItem(selected = selectedIndex == 3, onClick = { onItemSelected(3); onProfilClick() },
                icon = { Icon(NavItem.PROFIL.icon, contentDescription = NavItem.PROFIL.label, modifier = Modifier.size(24.dp)) },
                label = { Text(NavItem.PROFIL.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = PawPrimary, selectedTextColor = PawPrimary, indicatorColor = PawPrimaryLight))
        }
        FloatingActionButton(onClick = onPostingClick,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-22).dp).size(52.dp),
            shape = CircleShape, containerColor = PawPrimary, contentColor = Color.White) {
            Icon(Icons.Default.Add, contentDescription = "Posting Hewan", modifier = Modifier.size(26.dp))
        }
    }
}



// Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    PawLinkTheme {
        DashboardScreen()
    }
}