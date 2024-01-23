package agh.mobile.blurfacesmcc.ui.uploadvideo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.util.LinkedList

object PerformClustering {

    fun isSensitiveFaceInBitMapList(
        sensitiveFace: Bitmap?,
        frames: MutableList<Bitmap?>,
        names: MutableList<String>,
        context: Context?
    ): Boolean {
        frames.add(sensitiveFace)
        names.add("sensitive_face")
        val modelHandler = TfliteHandler(context, frames, names)
        val mEncodings = modelHandler.mEncodings
        val clusteringHandler = ClusteringHandler()
        clusteringHandler.KMeansClustering(mEncodings)
        val id = clusteringHandler.getKMeansClusterIdx(
            modelHandler.convertToEncoding(
                sensitiveFace,
                "sensitive_face"
            )
        )
        for (i in 0 until frames.size - 1) {
            if (clusteringHandler.getKMeansClusterIdx(
                    modelHandler.convertToEncoding(
                        frames[i],
                        names[i]
                    )
                ) == id
            ) {
                return true
            }
        }
        return false
    }

    fun getBitmapFromAssets(fileName: String?, context: Context): Bitmap? {
        val assetManager = context.assets
        var inputStream: InputStream? = null
        try {
            // Otwórz plik z assets
            inputStream = assetManager.open(fileName!!)

            // Wczytaj plik do Bitmapy
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}
