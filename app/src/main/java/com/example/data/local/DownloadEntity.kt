package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val url: String,
    val filePath: String,
    val fileName: String,
    val timestamp: Long
)
