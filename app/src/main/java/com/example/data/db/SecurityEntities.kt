package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val scanType: String,
    val totalAppsScanned: Int,
    val threatsFoundCount: Int,
    val securityScore: Int,
    val summaryAr: String,
    val threatDetailsJson: String = ""
)

@Entity(tableName = "trusted_apps")
data class TrustedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)
