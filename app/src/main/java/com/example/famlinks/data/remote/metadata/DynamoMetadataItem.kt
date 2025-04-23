//File: data/remote/metadata/DynamoMetadataItem.kt
package com.example.famlinks.data.remote.metadata

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*
import com.example.famlinks.data.remote.s3.Visibility

@DynamoDBTable(tableName = "PhotoMetadata")
class DynamoMetadataItem {

    @DynamoDBHashKey(attributeName = "identityId")
    var identityId: String? = null

    @DynamoDBRangeKey(attributeName = "photoKey")
    var photoKey: String? = null

    @DynamoDBAttribute
    var timestamp: Long? = null

    @DynamoDBAttribute
    var latitude: Double? = null

    @DynamoDBAttribute
    var longitude: Double? = null

    @DynamoDBAttribute
    var fileSizeBytes: Long? = null

    @DynamoDBAttribute
    var resolution: String? = null

    @DynamoDBAttribute
    var mediaType: String? = null

    // 🔧 New: Core org metadata
    @DynamoDBAttribute
    var isSingle: Boolean = false

    @DynamoDBAttribute
    var albumId: String? = null

    @DynamoDBAttribute
    var portalId: String? = null

    @DynamoDBAttribute
    var collectionIds: List<String>? = null

    // 🔐 Visibility and access
    @DynamoDBAttribute
    var visibility: String? = "PRIVATE"

    @DynamoDBAttribute
    var sharedWith: List<String>? = null

    @DynamoDBAttribute
    var ownerId: String? = null
}


