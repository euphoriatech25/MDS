package com.ramlaxmaninnovation.mds.views.ui.nurselist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Button
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
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.camera.BoundingBoxOverlay
import com.ramlaxmaninnovation.mds.camera.FrameAnalyser
import com.ramlaxmaninnovation.mds.database.User
import com.ramlaxmaninnovation.mds.database.UserDatabaseUtils
import com.ramlaxmaninnovation.mds.ml.EmbeddingUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import kotlinx.android.synthetic.main.activity_face_verification.*
import java.util.concurrent.Executors

class FaceVerificationActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var userFaceID: String
    private lateinit var patient_details: TextView
    private lateinit var status_face_verification: TextView
    private lateinit var show: TextView
    private lateinit var statusDisplay: CardView
    private lateinit var back_home: Button
    private lateinit var frameAnalyser: FrameAnalyser
    private lateinit var boundingBoxOverlay: BoundingBoxOverlay
    private var threshold = 0.9f
    private var userPrefManager: UserPrefManager? = null
    private lateinit var continue_verification: TextView

    private lateinit var continue_btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_verification)
        supportActionBar?.hide()
        changeStatusBarColor()
        userFaceID = intent.getStringExtra("faceID")!!
        threshold = intent.getFloatExtra("threshold", 0.9f)
        userPrefManager = UserPrefManager(this)

        Log.i("TAG", "onCreate: " + userFaceID)
        UserDatabaseUtils.getUsersFromFaceID(this, userFaceID, databaseCallback)

        show = findViewById(R.id.show)
        show.setText(R.string.face_show_user)
        patient_details = findViewById(R.id.patient_details)
        statusDisplay = findViewById(R.id.statusDisplay)
        status_face_verification = findViewById(R.id.status_face_verification)

        continue_verification = findViewById(R.id.continue_verification)
        previewView = findViewById(R.id.previewview_verificationactivity_camera_preview)
        boundingBoxOverlay = findViewById(R.id.boundingboxoverlay_verificationactivity_bbox_overlay)
        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)
        continue_verification.setOnClickListener {
            val intent = Intent(this, CameraViewActivity::class.java).apply {
            }
            startActivity(intent)

        }
        back_home = findViewById(R.id.back_home)

        back_home.setOnClickListener {
            val intent = Intent(this, CameraViewActivity::class.java).apply {
            }
            startActivity(intent)
        }

        continue_btn = findViewById(R.id.continue_btn)
//        continue_btn.visibility = View.VISIBLE
        continue_btn.setOnClickListener {
            val intent = Intent(this, CameraViewActivity::class.java).apply {
            }
            startActivity(intent)

        }

    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(com.ramlaxmaninnovation.mds.R.color.blue)
    }

    private val databaseCallback = object : UserDatabaseUtils.Companion.Callback {
        override fun onQueryCompleted(users: Array<User>) {
            val embeddings =
                users.map { EmbeddingUtils.stringToFloatArray(it.embedding!!, 128) }.toTypedArray()
            val name = users[0].personName!!
            val id = users[0].faceID!!
            val photo = users[0].photostring!!
            frameAnalyser = FrameAnalyser(
                this@FaceVerificationActivity,
                boundingBoxOverlay,
                embeddings,
                name,
                id,
                " ",
                statusDisplay,
                patient_details,
                status_face_verification,
                continue_verification,
                "user",
                photo,
                show,
                continue_btn
            )
            frameAnalyser.classificationThreshold = threshold
            startCameraPreview()

        }
    }

    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            },
            ContextCompat.getMainExecutor(this)
        )
    }


    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser)
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageFrameAnalysis
        )
    }


}