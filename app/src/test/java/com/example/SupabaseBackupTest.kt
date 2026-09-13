package com.example

import com.example.data.model.Car
import com.example.data.model.FuelRefill
import com.example.data.remote.BackupPayload
import com.example.data.remote.refillKey
import com.example.data.remote.validateBackup
import com.example.data.remote.vehicleKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SupabaseBackupTest {
    private val car = Car(id = 1, name = "Carry", regNumber = "MH 18 AB 1234",
        tankCapacityKg = 12.0, currentOdometer = 1000.0)
    private val refill = FuelRefill(id = 1, carId = 1, date = 1000,
        odometer = 1000.0, quantityKg = 5.0, pricePerKg = 95.0, totalAmount = 475.0)

    @Test fun registrationMatchingIgnoresCaseAndSpaces() {
        assertEquals(vehicleKey(car), vehicleKey(car.copy(regNumber = "mh18ab1234")))
    }

    @Test fun repeatRestoreRecognizesRefillsDespiteLocalIdChanges() {
        assertEquals(refillKey(refill), refillKey(refill.copy(id = 44, pumpId = null)))
        assertNotEquals(refillKey(refill), refillKey(refill.copy(date = 2000)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun orphanRefillRejectedBeforeRestore() {
        validateBackup(BackupPayload(cars = listOf(car), refills = listOf(refill.copy(carId = 9))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun ambiguousRegistrationRejectedBeforeRestore() {
        validateBackup(BackupPayload(cars = listOf(car, car.copy(id = 2, regNumber = "mh18ab1234")), refills = emptyList()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun futureFormatRejected() {
        validateBackup(BackupPayload(version = 2, cars = listOf(car), refills = listOf(refill)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidAmountRejected() {
        validateBackup(BackupPayload(cars = listOf(car), refills = listOf(refill.copy(quantityKg = Double.NaN))))
    }
}
