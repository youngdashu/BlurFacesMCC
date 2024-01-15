package agh.mobile.blurfacesmcc.ui.uploadvideo;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PerformClustering {

    public static void loadModelFile(Context context) throws IOException {

        List<Bitmap> frames = new LinkedList<>();
        List<String> names = new LinkedList<>();

        Bitmap img1 = getBitmapFromAssets("images.jpg",context);
        Bitmap img3 = getBitmapFromAssets("images1.jpg",context);
        Bitmap img2 = getBitmapFromAssets("449687.2.jpg",context);
        frames.add(img1);
        frames.add(img2);
        frames.add(img3);
        names.add("img1");
        names.add("img2");
        names.add("img3");

        frames.add(img1);
        names.add("img6");

        frames.add(img1);
        names.add("img7");

        frames.add(img1);
        names.add("img8");

        frames.add(img1);
        names.add("img9");

        frames.add(img1);
        names.add("img10");

        frames.add(img1);
        names.add("img10");

        frames.add(img1);
        names.add("img11");

        frames.add(img1);
        names.add("img12");

        frames.add(img1);
        names.add("img13");

        frames.add(img1);
        names.add("img13");

        frames.add(img1);
        names.add("img14");

        Log.d("Result of checking sensitivity", String.valueOf(isSensitivFaceInBitMapList(img2,frames,names,context)));
    }

    public static boolean isSensitivFaceInBitMapList(Bitmap sensitiveFace, List<Bitmap> frames, List<String> names, Context context){
        frames.add(sensitiveFace);
        names.add("sensitive_face");
        TfliteHandler modelHandler = new TfliteHandler(context,frames,names);

        HashMap<String, InferenceHelper.Encoding> mEncodings = modelHandler.mEncodings;

        ClusteringHandler clusteringHandler = new ClusteringHandler();


        clusteringHandler.KMeansClustering(mEncodings);
        int id = clusteringHandler.getKMeansClusterIdx(modelHandler.convertToEncoding(sensitiveFace,"sensitive_face"));
        for (int i =0;i<frames.size()-1;i++){
            if(clusteringHandler.getKMeansClusterIdx(modelHandler.convertToEncoding(frames.get(i),names.get(i))) == id){
                return true;
            }
        }
        return false;
    }

    static Bitmap getBitmapFromAssets(String fileName,Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;

        try {
            // Otwórz plik z assets
            inputStream = assetManager.open(fileName);

            // Wczytaj plik do Bitmapy
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
