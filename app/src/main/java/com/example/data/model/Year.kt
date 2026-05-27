package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Year(
    val id: String,
    val name: String,
    val description: String
)
