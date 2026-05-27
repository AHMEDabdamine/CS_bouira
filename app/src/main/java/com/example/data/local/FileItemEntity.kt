package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileItemEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val name: String,
    val type: String,
    val url: String,
    val size: Long?,
    val uploadedAt: String?,
    val uploader: String?
)
