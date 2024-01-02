package agh.mobile.blurfacesmcc.repositories.interfaces

import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest

interface VideosRepository {
    suspend fun processRemote(uploadVideoRequest: UploadVideoRequest)
}