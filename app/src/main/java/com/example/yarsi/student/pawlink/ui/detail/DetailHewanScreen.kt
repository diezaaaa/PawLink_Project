package com.example.yarsi.student.pawlink.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.yarsi.student.pawlink.data.repository.HewanModel
import com.example.yarsi.student.pawlink.ui.theme.PawAmber
import com.example.yarsi.student.pawlink.ui.theme.PawAmberLight
import com.example.yarsi.student.pawlink.ui.theme.PawBlue
import com.example.yarsi.student.pawlink.ui.theme.PawBlueLight
import com.example.yarsi.student.pawlink.ui.theme.PawLinkTheme
import com.example.yarsi.student.pawlink.ui.theme.PawPrimary
import com.example.yarsi.student.pawlink.ui.theme.PawPrimaryLight
import com.example.yarsi.student.pawlink.viewmodel.AuthViewModel
import com.example.yarsi.student.pawlink.viewmodel.HewanViewModel

enum class AnimalStatus(
    val label: String,
    val color: Color,
    val bgColor: Color,
    val icon: ImageVector
){
    ADOPSI(
        label = "Tersedia untuk Adopsi",
        color = PawPrimary,
        bgColor = PawPrimaryLight,
        icon = Icons.Outlined.Pets
    ),
    HILANG(
        label = "Hewan Hilang",
        color = PawAmber,
        bgColor = PawAmberLight,
        icon = Icons.Outlined.Warning
    ),
    DITEMUKAN(
        label = "Sudah ditemukan",
        color = PawBlue,
        bgColor = PawBlueLight,
        icon = Icons.Outlined.Check
    )
}

data class AnimalDetail(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val age: String = "",
    val gender: String = "",
    val description: String = "",
    val status: AnimalStatus = AnimalStatus.ADOPSI,
    val location: String = "",
    val contactName: String = "",
    val contactPhones: String = "",
    val postedAt: String = ""
)

@Composable
fun DetailHewanScreen(
    hewanId: String = "",
    hewanViewModel: HewanViewModel = viewModel(),
    onBack: () -> Unit = {},
    onAjukanAdopsi: () -> Unit = {},
    onChat: () -> Unit = {}
){

    val uiState by hewanViewModel.uiState.collectAsState()

    LaunchedEffect(hewanId) {
        if (hewanId.isNotBlank()) {
            hewanViewModel.loadHewanDetail(hewanId)
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PawPrimary)
        }
        return
    }
    val contact = uiState.selectedHewanContact
    val animal = uiState.selectedHewan?.toAnimalDetail(
        contactName  = contact?.first ?: "",
        contactPhone = contact?.second ?: ""
    ) ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Box scrollable untuk konten
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HeroPhotoSection(
                animal = animal,
                photoUrl = uiState.selectedHewanPhotoUrl,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 300.dp)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection(animal = animal)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                InfoGridSection(animal = animal)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Description(animal = animal)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                LocationSection(animal = animal)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ContactSection(animal = animal)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                if (animal.status == AnimalStatus.DITEMUKAN) FoundBanner()
                Spacer(modifier = Modifier.height(
                    if (animal.status != AnimalStatus.DITEMUKAN) 80.dp else 16.dp
                ))
            }
        }

        // Action buttons menempel di bawah — di dalam BoxScope
        if (animal.status != AnimalStatus.DITEMUKAN) {
            ActionButtons(
                animal = animal,
                onAjukanAdopsi = onAjukanAdopsi,
                onChat = onChat,
                modifier = Modifier.align(Alignment.BottomCenter) // ← valid di sini
            )
        }
    }
}

@Composable
fun HeaderSection(animal: AnimalDetail){
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(animal.status.bgColor)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ){
            Icon(
                animal.status.icon,
                contentDescription = null,
                tint = animal.status.color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                animal.status.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = animal.status.color
            )
        }

        Text(
            animal.name.ifBlank { "Nama tidak diketahui" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                "Diposting ${animal.postedAt.ifBlank{ "baru saja" }}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HeroPhotoSection(
    animal: AnimalDetail,
    photoUrl: String = "",
    onBack: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .background(MaterialTheme.colorScheme.primaryContainer)
    ){
        if (photoUrl.isNotBlank()) {
            // Tampilkan foto dari Storage
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto hewan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder kalau tidak ada foto
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Icon(
                Icons.Outlined.Pets,
                contentDescription = "null",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 32.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
        ){
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ){
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 20.dp else 8.dp,8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == 0) MaterialTheme.colorScheme.primary
                            else Color.White.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
fun InfoGridSection(animal: AnimalDetail){
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)){
        Text(
            "Informasi Hewan",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            InfoChip(
                label = "Jenis",
                value = animal.type.ifBlank { "-" },
                icon = Icons.Outlined.Pets,
                modifier = Modifier.weight(1f)
            )
            InfoChip(
                label = "Ras",
                value = animal.breed.ifBlank { "-" },
                icon = Icons.Outlined.Category,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            InfoChip(
                label = "Usia",
                value = animal.age.ifBlank { "-" },
                icon = Icons.Outlined.CalendarMonth,
                modifier = Modifier.weight(1f)
            )

            InfoChip(
                label = "Jenis Kelamin",
                value = animal.gender.ifBlank { "-" },
                icon = Icons.Outlined.Female,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun InfoChip(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
){
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column{
                Text(
                    label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun Description(animal: AnimalDetail){
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Deskripsi",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            animal.description.ifBlank { "Tidak ada deskripsi." },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun LocationSection(animal: AnimalDetail){
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Lokasi",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            verticalAlignment =  Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                animal.location.ifBlank { "Lokasi tidak diketahui" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun ContactSection(animal: AnimalDetail) {
    val contactLabel = when (animal.status) {
        AnimalStatus.ADOPSI -> "Hubungi Pencari"
        AnimalStatus.HILANG -> "Hubungi Pelapor"
        AnimalStatus.DITEMUKAN -> "Hubungi Penemu"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text(
            "Kontak ${contactLabel}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)){
                    Text(
                        animal.contactName.ifBlank { "Nama tidak diketahui" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        animal.contactPhones.ifBlank { "Nomor telepon tidak diketahui" },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        contactLabel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ){
                    Text(
                        "Member",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun FoundBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F1FB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF378ADD),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "Hewan Sudah Ditemukan!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF378ADD)
                )
                Text(
                    "Terima kasih kepada komunitas PawLink yang sudah membantu.",
                    fontSize = 13.sp,
                    color = Color(0xFF378ADD).copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    animal: AnimalDetail,
    onAjukanAdopsi: () -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (animal.status) {
                AnimalStatus.ADOPSI -> {
                    // Chat ke pemilik
                    OutlinedButton(
                        onClick = onChat,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Chat Pemilik",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Ajukan adopsi
                    Button(
                        onClick = onAjukanAdopsi,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Ajukan Adopsi",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AnimalStatus.HILANG -> {
                    // Chat ke pelapor (full width)
                    Button(
                        onClick = onChat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PawAmber
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Chat ke Pelapor",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                AnimalStatus.DITEMUKAN -> {
                }
            }
        }
    }
}


fun HewanModel.toAnimalDetail(
    contactName: String = "",
    contactPhone: String = ""
): AnimalDetail {
    val animalStatus = when (this.status.lowercase()) {
        "tersedia"  -> AnimalStatus.ADOPSI
        "hilang"    -> AnimalStatus.HILANG
        "ditemukan" -> AnimalStatus.DITEMUKAN
        else        -> AnimalStatus.ADOPSI
    }
    return AnimalDetail(
        id          = this.id,
        name        = this.name,
        type        = this.type,
        breed       = this.breed,
        age         = this.age,
        gender      = this.gender,
        description = this.description,
        status      = animalStatus,
        location    = this.location, // belum ada field lokasi di HewanModel sebagai string
        contactName = contactName, // perlu ambil dari collection users berdasarkan userId
        contactPhones = contactPhone,
        postedAt    = this.createdAt
    )
}
fun sampleAnimal(status: AnimalStatus = AnimalStatus.ADOPSI) = AnimalDetail(
    id = "1",
    name = "Mochi",
    type = "Kucing",
    breed = "Persia",
    age = "2 Tahun",
    gender = "Perempuan",
    description = "Mochi adalah kucing persia yang sangat ramah dan suka bermain. " +
            "Sudah divaksin lengkap dan jinak dengan anak kecil. " +
            "Butuh rumah baru yang penuh kasih sayang.",
    status = status,
    location = "Kebayoran Baru, Jakarta Selatan",
    contactName = "Alfaridzi Dieza",
    contactPhones = "08123456789",
    postedAt = "2 jam yang lalu"
)

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun DetailAdopsiPreview() {
//    PawLinkTheme {
//        DetailHewanScreen(animal = sampleAnimal(AnimalStatus.ADOPSI))
//    }
//}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HeroPhotoSectionPreview(){
    PawLinkTheme{
        HeroPhotoSection(animal = sampleAnimal(AnimalStatus.ADOPSI)) {

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HeaderSectionPreview(){
    PawLinkTheme{
        HeaderSection(animal = sampleAnimal(AnimalStatus.ADOPSI))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InfoGridSectionPreview(){
    PawLinkTheme{
        InfoGridSection(animal = sampleAnimal(AnimalStatus.ADOPSI))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InfoChipPreview(){
    PawLinkTheme {
        InfoChip(label = "Jenis Kelamin", value = "Jantan", icon = Icons.Outlined.Male)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DescriptionPreview(){
    PawLinkTheme{
        Description(animal = sampleAnimal(AnimalStatus.ADOPSI))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LocationSectionPreview(){
    PawLinkTheme{
        LocationSection(animal = sampleAnimal(AnimalStatus.ADOPSI))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ContactSectionPreview(){
    PawLinkTheme{
        ContactSection(animal = sampleAnimal(AnimalStatus.ADOPSI))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoundBannerPreview(){
    PawLinkTheme{
        FoundBanner()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActionButtonsPreview(){
    PawLinkTheme{
        ActionButtons(animal = sampleAnimal(AnimalStatus.ADOPSI), onAjukanAdopsi = {}, onChat = {})
    }
}