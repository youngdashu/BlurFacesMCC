package agh.mobile.blurfacesmcc.repositories

import agh.mobile.blurfacesmcc.dataSources.RemoteVideoDataSource
import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.repositories.interfaces.VideosRepository
import javax.inject.Inject

class DefaultVideosRepository @Inject constructor(
    private val remoteVideoDataSource: RemoteVideoDataSource
) : VideosRepository {
    override suspend fun processRemote(uploadVideoRequest: UploadVideoRequest) =
        remoteVideoDataSource.upload(uploadVideoRequest).body()!!
}