package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityDao {
    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    fun getAllScanRecords(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestScanRecord(): ScanRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanRecord(record: ScanRecordEntity): Long

    @Query("DELETE FROM scan_records")
    suspend fun clearScanHistory()

    // Trusted apps whitelist
    @Query("SELECT * FROM trusted_apps")
    fun getAllTrustedApps(): Flow<List<TrustedAppEntity>>

    @Query("SELECT packageName FROM trusted_apps")
    suspend fun getTrustedPackageNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrustedApp(trustedApp: TrustedAppEntity)

    @Query("DELETE FROM trusted_apps WHERE packageName = :packageName")
    suspend fun removeTrustedApp(packageName: String)
}
