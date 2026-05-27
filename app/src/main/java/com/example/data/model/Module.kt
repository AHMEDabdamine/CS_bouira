package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Module(
    val id: String,
    val name: String,
    val code: String,
    val yearId: String,
    val semester: Int // 1 or 2
)
