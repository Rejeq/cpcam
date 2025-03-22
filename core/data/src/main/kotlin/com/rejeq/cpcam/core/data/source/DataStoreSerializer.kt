package com.rejeq.cpcam.core.data.source

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.rejeq.cpcam.data.datastore.AppPreferences
import java.io.InputStream
import java.io.OutputStream

class DataStoreSerializer : Serializer<AppPreferences> {
    override val defaultValue = AppPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppPreferences {
        try {
            return AppPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppPreferences, output: OutputStream) =
        t.writeTo(output)
}
