package com.example.famlinks.data.remote.metadata

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*

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
}

