package com.example.famlinks.data.remote.metadata

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*

@DynamoDBTable(tableName = "PhotoMetadata")
class DynamoMetadataItem {
    @get:DynamoDBHashKey(attributeName = "identityId")
    @set:DynamoDBHashKey(attributeName = "identityId")
    var identityId: String? = null

    @get:DynamoDBRangeKey(attributeName = "photoKey")
    @set:DynamoDBRangeKey(attributeName = "photoKey")
    var photoKey: String? = null

    @DynamoDBAttribute
    var timestamp: Long? = null

    @DynamoDBAttribute
    var latitude: Double? = null

    @DynamoDBAttribute
    var longitude: Double? = null
}
