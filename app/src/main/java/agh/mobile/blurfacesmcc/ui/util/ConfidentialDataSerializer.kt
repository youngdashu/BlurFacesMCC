package agh.mobile.blurfacesmcc.ui.util

import agh.mobile.blurfacesmcc.ConfidentialDataArray
import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ConfidentialDataSerializer : Serializer<ConfidentialDataArray> {
    override val defaultValue: ConfidentialDataArray = ConfidentialDataArray.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ConfidentialDataArray {
        try {
            return ConfidentialDataArray.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ConfidentialDataArray,
        output: OutputStream
    ) = t.writeTo(output)
}

val Context.confidentialDataArrayStore: DataStore<ConfidentialDataArray> by dataStore(
    fileName = "confidential_data_array.pb",
    serializer = ConfidentialDataSerializer
)