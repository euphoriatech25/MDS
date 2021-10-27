package com.ramlaxmaninnovation.mds.ml

import android.content.Context
import android.graphics.Bitmap
import android.media.ExifInterface
import android.util.Log
import com.app.ramlaxmangroup.face_recognition.ml.FaceNetModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.ramlaxmaninnovation.mds.camera.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class EmbeddingGenerator( context : Context )  {

    private val faceNetModel = FaceNetModel( context )
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode( FaceDetectorOptions.PERFORMANCE_MODE_FAST )
        .build()
    private val faceDetector = FaceDetection.getClient(realTimeOpts)
    private lateinit var callback: Callback
    private val coroutineScope = CoroutineScope( Dispatchers.Main )



    interface Callback {

        fun onEmbeddingGenerated( embedding : String )

        fun onMultipleFacesFound( numFaces : Int )

        fun onNoFacesFound()

        fun onError( exception: Exception )

    }


    fun getFaceEmbedding( image : Bitmap , imageExifInterface: ExifInterface , callback: Callback) {
        this.callback = callback
        val bitmap =
            when ( imageExifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION , ExifInterface.ORIENTATION_UNDEFINED )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> BitmapUtils.rotateBitmap(image, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> BitmapUtils.rotateBitmap(image, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> BitmapUtils.rotateBitmap(image, 270f)
                else -> image
            }
        detectFaces( bitmap )
    }


    private fun detectFaces( image : Bitmap ) {
        val inputImage = InputImage.fromBitmap( image , 0 )
        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                when (faces.size) {
                    1 -> {
                       try {
                           val croppedFace = BitmapUtils.cropBitmap(image, faces[0].boundingBox)
                           coroutineScope.launch {
                               val embedding = faceNetModel.getFaceEmbedding( croppedFace )
                               callback.onEmbeddingGenerated(
                                   EmbeddingUtils.floatArrayToString(
                                       embedding
                                   )
                               )
                           }
                       } catch (e:IllegalAccessException ){
                          e.printStackTrace()
                           callback.onNoFacesFound()
                           Log.i("TAG", "detectFaces: "+e.localizedMessage)
                       }
                    }
                    0 -> {
                        callback.onNoFacesFound()
                    }
                    else -> {
                        callback.onMultipleFacesFound( faces.size )
                    }
                }
            }
            .addOnFailureListener {
                callback.onError( it )
            }
    }

}