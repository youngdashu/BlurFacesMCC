package agh.mobile.blurfacesmcc.domain.requestTypes

data class UploadVideoRequest(
    val file: ByteArray,
    val fileName: String
)
