package com.example.data.remote

import androidx.room.withTransaction
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.Car
import com.example.data.model.FuelRefill
import com.example.util.SupabaseSession
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Manual, immutable backups. Supabase Auth verifies account ownership. */
class SupabaseBackup(private val database: AppDatabase) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    private val adapter = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        .adapter(BackupPayload::class.java)

    private suspend fun session(): Pair<String, String> = SupabaseSession.credentials()

    private fun checkUser(uid: String) {
        check(SupabaseSession.currentUser?.uid == uid) {
            "Account changed. Please retry from your signed-in account."
        }
    }

    private fun request(token: String, body: String? = null): String {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/').toHttpUrl()
        require(base.isHttps && base.username.isEmpty() && base.password.isEmpty()) {
            "Supabase URL must use HTTPS."
        }
        val url = base.newBuilder().addPathSegments("rest/v1/cng_mitra_backups")
        if (body == null) {
            url.addQueryParameter("select", "payload")
                .addQueryParameter("order", "created_at.desc,id.desc")
                .addQueryParameter("limit", "1")
        }
        val builder = Request.Builder().url(url.build())
            .header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            .header("Authorization", "Bearer $token")
        if (body != null) builder.header("Prefer", "return=minimal")
            .post(body.toRequestBody("application/json".toMediaType()))
        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException(when (response.code) {
                401, 403 -> "Supabase rejected login. Sign in again and check account permissions."
                404 -> "Supabase backup table is not installed."
                else -> "Supabase backup request failed (HTTP ${response.code}). Please retry."
            })
            // Do not log the response, access token, or the user's backup.
            return response.body?.string().orEmpty()
        }
    }

    suspend fun backup(): String = withContext(Dispatchers.IO) {
        val (uid, token) = session()
        val payload = database.withTransaction {
            BackupPayload(cars = database.carDao().getAllCars().first(),
                refills = database.refillDao().getAllRefills().first())
        }
        validateBackup(payload)
        check(payload.cars.isNotEmpty() || payload.refills.isNotEmpty()) { "No vehicles or refills to back up." }
        val json = adapter.toJson(payload)
        check(json.toByteArray(Charsets.UTF_8).size <= 4_000_000) {
            "Backup is too large. Use the file export option."
        }
        checkUser(uid)
        request(token, JSONObject().put("user_id", uid).put("payload", JSONObject(json)).toString())
        "Supabase backup saved: ${payload.cars.size} vehicles, ${payload.refills.size} refills."
    }

    suspend fun restore(): String = withContext(Dispatchers.IO) {
        val (uid, token) = session()
        val rows = JSONArray(request(token))
        check(rows.length() > 0) { "No Supabase backup found for this account." }
        val payload = adapter.fromJson(rows.getJSONObject(0).getJSONObject("payload").toString())
            ?: error("Invalid backup.")
        validateBackup(payload)
        checkUser(uid)
        var addedCars = 0
        var addedRefills = 0
        database.withTransaction {
            val cars = database.carDao().getAllCars().first().toMutableList()
            val refills = database.refillDao().getAllRefills().first().toMutableList()
            val carIds = mutableMapOf<Long, Long>()
            for (car in payload.cars) {
                val matches = cars.filter { vehicleKey(it) == vehicleKey(car) }
                check(matches.size <= 1) { "Duplicate vehicle registrations found. Resolve them before restoring." }
                val existing = matches.singleOrNull()
                carIds[car.id] = if (existing != null) existing.id else {
                    val restored = car.copy(id = 0, isDefault = cars.isEmpty())
                    val id = database.carDao().insertCar(restored)
                    cars.add(restored.copy(id = id))
                    addedCars++
                    id
                }
            }
            for (refill in payload.refills) {
                val mapped = refill.copy(id = 0, carId = carIds.getValue(refill.carId), pumpId = null)
                if (refills.none { refillKey(it) == refillKey(mapped) }) {
                    database.refillDao().insertRefill(mapped)
                    refills.add(mapped)
                    addedRefills++
                }
            }
            checkUser(uid)
        }
        "Supabase restore complete: $addedCars vehicles, $addedRefills refills added. Existing entries kept."
    }
}

data class BackupPayload(val version: Int = 1, val cars: List<Car>, val refills: List<FuelRefill>)

internal fun vehicleKey(car: Car): String = car.regNumber.filterNot(Char::isWhitespace).uppercase(java.util.Locale.ROOT)

internal fun refillKey(refill: FuelRefill): List<Any> = listOf(
    refill.carId, refill.date, refill.odometer, refill.quantityKg, refill.totalAmount
)

internal fun validateBackup(payload: BackupPayload) {
    require(payload.version == 1) { "Unsupported backup version." }
    require(payload.cars.all { it.id > 0 && vehicleKey(it).isNotBlank() }) { "Every vehicle needs a registration number." }
    require(payload.cars.map { it.id }.distinct().size == payload.cars.size) { "Duplicate vehicle IDs in backup." }
    require(payload.cars.map(::vehicleKey).distinct().size == payload.cars.size) { "Duplicate registrations in backup." }
    val ids = payload.cars.map { it.id }.toSet()
    require(payload.refills.all { it.carId in ids }) { "Backup contains a refill without its vehicle." }
    require(payload.refills.all {
        listOf(it.odometer, it.quantityKg, it.pricePerKg, it.totalAmount, it.distanceTravelled,
            it.mileageKmPerKg, it.costPerKm).all { value -> value.isFinite() && value >= 0 }
    }) { "Backup contains invalid refill amounts." }
}
