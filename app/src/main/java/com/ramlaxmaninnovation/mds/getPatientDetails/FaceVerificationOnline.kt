package com.ramlaxmaninnovation.mds.getPatientDetails

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.ramlaxmaninnovation.mds.camera.BoundingBoxOverlay
import com.ramlaxmaninnovation.mds.camera.FrameAnalyser
import com.ramlaxmaninnovation.mds.ml.EmbeddingUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import java.util.concurrent.Executors


import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity

class FaceVerificationOnline : AppCompatActivity(), GetDetails.CallbackOnline {

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView : PreviewView
    private lateinit var userFaceID : String
    private lateinit var patient_details : TextView
    private lateinit var continue_verification : TextView
    private lateinit var status_face_verification : TextView
    private lateinit var statusDisplay : CardView
    private lateinit var frameAnalyser: FrameAnalyser
    private lateinit var boundingBoxOverlay: BoundingBoxOverlay
    private var threshold = 0.9f

    private var userPrefManager: UserPrefManager? = null

    private lateinit var continue_btn : Button
    private lateinit var progress : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_verification)
        supportActionBar?.hide()
        changeStatusBarColor()
        userFaceID = intent.getStringExtra( "faceID" )!!
//        threshold = intent.getFloatExtra( "threshold" , 0.9f )
        userPrefManager = UserPrefManager(this)
        threshold= userPrefManager!!.threshold.toFloat()


        Log.i("TAG", "onCreate: "+  userPrefManager!!.threshold)

        var a=intent.getStringExtra("NOT_MATCH")


        Log.i("TAG", "onCreate: "+a)


        progress = findViewById(R.id.progress)
         continue_btn = findViewById(R.id.continue_btn)
        continue_btn.visibility= View.VISIBLE
        continue_btn.setOnClickListener {
            val intent = Intent(this, CameraViewActivity::class.java).apply {
            }
            startActivity(intent)

        }

        GetDetails.getUserDetails(this,userFaceID,progress)
        patient_details = findViewById(R.id.patient_details)
        statusDisplay = findViewById(R.id.statusDisplay)
        status_face_verification = findViewById(R.id.status_face_verification)

        continue_verification = findViewById(R.id.continue_verification)
        previewView = findViewById(R.id.previewview_verificationactivity_camera_preview)
        boundingBoxOverlay = findViewById(R.id.boundingboxoverlay_verificationactivity_bbox_overlay)
        boundingBoxOverlay.setWillNotDraw( false )
        boundingBoxOverlay.setZOrderOnTop( true )
        continue_verification.setOnClickListener {
            val intent = Intent(this, CameraViewActivity::class.java).apply {
            }
            startActivity(intent)

        }
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance( this )
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider) },
            ContextCompat.getMainExecutor(this) )
    }

    //TODO

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()
        Log.i("TAG", "bindPreview: "+userPrefManager?.cameraView)
        try {
            if(userPrefManager?.cameraView.equals("front")){
                val cameraSelector : CameraSelector = CameraSelector.Builder()
                    .requireLensFacing( CameraSelector.LENS_FACING_FRONT )
                    .build()
                preview.setSurfaceProvider( previewView.surfaceProvider )
                val imageFrameAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution( Size( 480, 640 ) )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser )
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview , imageFrameAnalysis )
            }else{
                val cameraSelector : CameraSelector = CameraSelector.Builder()
                    .requireLensFacing( CameraSelector.LENS_FACING_BACK )
                    .build()
                preview.setSurfaceProvider( previewView.surfaceProvider )
                val imageFrameAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution( Size( 480, 640 ) )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser )
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview , imageFrameAnalysis )
            }

        }catch (e:IllegalArgumentException ){}


    }

    override fun onQueryCompleted(users: GetDetailsModel.Data?) {
        if(users!=null)
        {
            val embeddings = users.photo.map{ EmbeddingUtils.stringToFloatArray( users.photo!! , 128 ) }.toTypedArray()
            val name = users.name!!
            val id= users.patientId!!
            Log.i("TAG", "onQueryCompleted: $name$id$embeddings")
            frameAnalyser = FrameAnalyser( this@FaceVerificationOnline , boundingBoxOverlay , embeddings , name ,id,"remarks",statusDisplay ,patient_details,status_face_verification,continue_verification,"verify","")
            frameAnalyser.classificationThreshold = threshold
            startCameraPreview()
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, CameraViewActivity::class.java).apply {
        }
        startActivity(intent)
        finish()
    }

    override fun onQueryError(users: GetDetailsModel.Error?) {
        if (users != null) {
            Log.i("TAG", "onQueryError: "+users.message)
        }
    }


}