package com.box.picker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "packages")
data class ExpressPackage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationName: String, // 驿站名称
    val providerName: String, // 服务商名�?
    val pickupCode: String,
    val address: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val originalText: String
)

@Entity(tableName = "rules")
data class RegexRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String, // 驿站名称
    val identificationKeywords: String, // 识别关键�?(e.g., "菜鸟,驿站")
    val providerName: String, // 服务商名�?
    val matchingRules: String, // 匹配规则 (newline separated regexes)
    val isEnabled: Boolean = true,
    val priority: Int = 0
)

@Dao
interface ExpressDao {
    @Query("SELECT * FROM packages WHERE isArchived = 0 ORDER BY timestamp DESC")
    fun getPendingPackages(): Flow<List<ExpressPackage>>

    @Query("SELECT * FROM packages WHERE isArchived = 1 ORDER BY timestamp DESC")
    fun getArchivedPackages(): Flow<List<ExpressPackage>>

    @Insert
    suspend fun insertPackage(pkg: ExpressPackage)

    @Update
    suspend fun updatePackage(pkg: ExpressPackage)

    @Delete
    suspend fun deletePackage(pkg: ExpressPackage)

    @Query("UPDATE packages SET isArchived = 1, timestamp = :time WHERE id = :id")
    suspend fun archivePackage(id: Long, time: Long = System.currentTimeMillis())

    @Query("UPDATE packages SET isArchived = 0 WHERE id = :id")
    suspend fun restorePackage(id: Long)

    @Query("DELETE FROM packages WHERE isArchived = 1")
    suspend fun clearArchivedPackages()

    @Query("DELETE FROM packages WHERE isArchived = 1 AND timestamp < :threshold")
    suspend fun deleteOldArchivedPackages(threshold: Long)

    @Query("SELECT * FROM rules ORDER BY priority DESC")
    suspend fun getAllRules(): List<RegexRule>
    
    @Insert
    suspend fun insertRule(rule: RegexRule)

    @Delete
    suspend fun deleteRule(rule: RegexRule)
}

@Database(entities = [ExpressPackage::class, RegexRule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expressDao(): ExpressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "express-db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


