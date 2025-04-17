package com.example.famlinks.data.remote.s3

data class S3Photo(
    val key: String,  // S3 object key (used to lookup metadata)
    val url: String   // Presigned URL for display
)
