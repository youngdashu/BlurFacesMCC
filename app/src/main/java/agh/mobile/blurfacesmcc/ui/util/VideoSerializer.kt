package agh.mobile.blurfacesmcc.ui.util

import agh.mobile.blurfacesmcc.Videos
import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object VideoSerializer : Serializer<Videos> {
    override val defaultValue: Videos = Videos.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Videos {
        try {
            return Videos.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: Videos,
        output: OutputStream
    ) = t.writeTo(output)
}

val Context.videoDataStore: DataStore<Videos> by dataStore(
    fileName = "videos.pb",
    serializer = VideoSerializer
)