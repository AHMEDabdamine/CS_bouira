package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "years")
data class YearEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String
)
