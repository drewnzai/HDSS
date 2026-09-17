package com.andrew.hdss.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.data.daos.MembershipDao
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.Location

@Database(
    entities = [
        Location::class,
        Individual::class,
        Household::class,
        Membership::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun individualDao(): IndividualDao
    abstract fun householdDao(): HouseholdDao
    abstract fun membershipDao(): MembershipDao

    companion object {
        private const val DATABASE_NAME = "hdss.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}