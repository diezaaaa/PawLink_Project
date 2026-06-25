package com.example.yarsi.student.pawlink.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LokasiResult(
    val latitude: Double,
    val longitude: Double,
    val alamat: String
)

object LocationHelper {

    // Cek apakah permission lokasi sudah diberikan
    fun isLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun hitungJarak(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // Ambil lokasi saat ini
    suspend fun getCurrentLocation(context: Context): Result<LokasiResult> {
        if (!isLocationPermissionGranted(context)) {
            return Result.failure(Exception("Izin lokasi belum diberikan."))
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationToken = CancellationTokenSource()

            val location = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener { e ->
                    continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    cancellationToken.cancel()
                }
            }

            if (location == null) {
                Result.failure(Exception("Gagal mendapatkan lokasi. Pastikan GPS aktif."))
            } else {
                val alamat = getAlamatFromKoordinat(
                    context,
                    location.latitude,
                    location.longitude
                )
                Result.success(
                    LokasiResult(
                        latitude  = location.latitude,
                        longitude = location.longitude,
                        alamat    = alamat
                    )
                )
            }
        } catch (e: SecurityException) {
            Result.failure(Exception("Izin lokasi ditolak."))
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mendapatkan lokasi: ${e.message}"))
        }
    }

    // Konversi koordinat ke nama alamat
    private fun getAlamatFromKoordinat(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        return try {
            val geocoder = Geocoder(context, Locale("id", "ID"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ pakai listener
                var hasil = ""
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    hasil = formatAlamat(addresses.firstOrNull()) ?: ""
                }
                // Fallback kalau listener belum selesai
                if (hasil.isBlank()) "$latitude, $longitude" else hasil
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                formatAlamat(addresses?.firstOrNull())
                    ?: "$latitude, $longitude"
            }
        } catch (e: Exception) {
            "$latitude, $longitude" // fallback ke koordinat
        }
    }

    // Format alamat
    private fun formatAlamat(address: android.location.Address?): String? {
        if (address == null) return null

        val parts = mutableListOf<String>()

        // Ambil nama jalan / kelurahan / kecamatan / kota
        address.subLocality?.let { parts.add(it) }      // Kelurahan/Kecamatan
        address.locality?.let { parts.add(it) }          // Kota
        address.adminArea?.let { parts.add(it) }          // Provinsi

        return if (parts.isNotEmpty()) parts.take(2).joinToString(", ")
        else address.getAddressLine(0) ?: null
    }
}