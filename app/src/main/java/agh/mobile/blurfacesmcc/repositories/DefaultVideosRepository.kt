package agh.mobile.blurfacesmcc.repositories

import agh.mobile.blurfacesmcc.dataSources.RemoteVideoDataSource
import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.repositories.interfaces.VideosRepository
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class DefaultVideosRepository @Inject constructor(
    private val remoteVideoDataSource: RemoteVideoDataSource
) : VideosRepository {
    override suspend fun processRemote(uploadVideoRequest: UploadVideoRequest) =
        try {
            remoteVideoDataSource.upload(uploadVideoRequest).body()!!
        } catch (e: Exception) {
            throw CancellationException(IOException())
        }
}