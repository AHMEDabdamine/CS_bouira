package com.example.data.api

import com.google.gson.annotations.SerializedName

data class DriveResponse(
    val link: String?,
    val files: List<DriveFile>?,
    val subfolders: Map<String, DriveResponse>?
)

data class DriveFile(
    val name: String,
    val link: String,
    val previewLink: String,
    val downloadLink: String
)
