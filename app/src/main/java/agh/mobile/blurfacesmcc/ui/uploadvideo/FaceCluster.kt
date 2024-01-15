package agh.mobile.blurfacesmcc.ui.uploadvideo

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import java.io.IOException

@HiltViewModel
class FaceCluster @Inject constructor() : ViewModel() {

    private lateinit var context: Context;

    fun postContext(context:Context){
        this.context=context;
    }

    fun isTheSameFace(photo1: Any, framesOfVideo: Any): Boolean {
        PerformClustering.loadModelFile(this.context)



        return true;
    }

}