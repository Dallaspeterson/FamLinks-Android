package com.example.famlinks.data.remote.metadata

data class PhotoMetadata(
    val identityId: String,
    val photoKey: String,
    val timestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)
