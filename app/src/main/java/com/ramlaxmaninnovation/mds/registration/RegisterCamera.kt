package com.ramlaxmaninnovation.mds.registration

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.home.getAspectRatio
import com.ramlaxmaninnovation.home.showToast
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.CameraViewBinding
import com.ramlaxmaninnovation.mds.getPatientDetails.CheckAvailability
import com.ramlaxmaninnovation.mds.getPatientDetails.FaceVerificationOnline
import com.ramlaxmaninnovation.mds.getPatientDetails.GetDetailsModel
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientDetailsFragment

class RegisterCamera  : AppCompatActivity(), CheckAvailability.CallbackAvailability {

    val TAG = "MainActivity"

    private lateinit var camera: Camera
    private lateinit var cameraProvider: ProcessCameraProvider

    var splitted: Array<String>? = null
    private lateinit var binding: CameraViewBinding

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1003
    }

    private val screenAspectRatio by lazy {
        val metrics = DisplayMetrics().also { binding.previewView.display.getRealMetrics(it) }
        metrics.getAspectRatio()
    }
    private  var destination:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CameraViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.openMenu.visibility= View.GONE
        binding.openMenu.setOnClickListener {
            val intent = Intent(
                this@RegisterCamera,
                PatientDetailsFragment::class.java
            )
            startActivity(intent)
        }


        val userPrefManager: UserPrefManager = UserPrefManager(this)
        binding.location.setText(getString(R.string.location)+" :- "+userPrefManager.location)
        binding.nurseDetails.setText(getString(R.string.user_name)+" :- "+userPrefManager.nurseDetails[1])
        binding.logoutNurse.visibility=View.GONE
        val decodedString: ByteArray = Base64.decode((userPrefManager.nurseDetails[2]), Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        Glide.with(this)
            .load(decodedByte)
            .apply(RequestOptions().circleCrop())
            .into(binding.nurseImage)


        changeStatusBarColor()
        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }

        binding.imgFlashLight.setOnClickListener {
            if (camera != null) {
                if (camera.cameraInfo.torchState.value == TorchState.ON) {
                    setFlashOffIcon()
                    camera.cameraControl.enableTorch(false)
                } else {
                    setFlashOnIcon()
                    camera.cameraControl.enableTorch(true)
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindPreview(camProvider: ProcessCameraProvider) {
        try {
            cameraProvider = camProvider

            val previewUseCase = Preview.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .setTargetAspectRatio(screenAspectRatio)
                .build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            val barcodeScanner = BarcodeScanning.getClient()
            val analysisUseCase = ImageAnalysis.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .setTargetAspectRatio(screenAspectRatio)
                .build().also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        { imageProxy ->
                            processImageProxy(barcodeScanner, imageProxy)
                        }
                    )
                }
            val useCaseGroup = UseCaseGroup.Builder().addUseCase(previewUseCase).addUseCase(
                analysisUseCase
            ).build()

            camera = cameraProvider.bindToLifecycle(
                this,
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(),
                useCaseGroup
            )
        } catch (e: NullPointerException) {
            e.printStackTrace()
            startCamera()
        }

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {

        // This scans the entire screen for barcodes
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    if (!barcodeList.isNullOrEmpty()) {
                        if (!barcodeList[0].rawValue.isNullOrEmpty()) {
                            Log.e(TAG, "processImageProxy: " + barcodeList[0].rawValue)
                            cameraProvider.unbindAll()
                            setFlashOffIcon()
                            if(barcodeList.isNotEmpty()){
                                Snackbar.make(
                                    this@RegisterCamera, binding.clMain,
                                    "${barcodeList[0].rawValue!!}", Snackbar.LENGTH_INDEFINITE
                                )
                                    .setAction(getString(R.string.register_patient)) {
//                                    startCamera()
                                        splitted = barcodeList[0].rawValue!!.split("???").toTypedArray()


                                        if (AppUtils.isNetworkAvailable(this)) {
                                            CheckAvailability.getUserDetails(
                                                this@RegisterCamera,
                                                splitted!![0]
                                            )

                                        } else {
                                            Toast.makeText(
                                                this@RegisterCamera,
                                                getString(R.string.no_interest_connection),
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
                                    }
                                    .show()
                            }else{
                                Toast.makeText(this@RegisterCamera,getString(R.string.invalid_qr),Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }.addOnFailureListener {
                    image.close()
                    imageProxy.close()
                    Log.e(TAG, "processImageProxy: ", it)
                }.addOnCompleteListener {
                    image.close()
                    imageProxy.close()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        setFlashOffIcon()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(
            this@RegisterCamera,
            PatientDetailsFragment::class.java
        )

        startActivity(intent)
        finish()
    }

    private fun isCameraPermissionGranted(): Boolean {
        val selfPermission =
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
        return selfPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (isCameraPermissionGranted()) {
                startCamera()
            } else {
                //show custom dialog of camera permission if permission is permanently denied
                showToast("Please allow camera permission!")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (isCameraPermissionGranted()) {
                    startCamera()
                } else {
                    showToast("Please allow camera permission!")
                }
            }
        }
    }

    private fun setFlashOffIcon() {
        binding.imgFlashLight.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_flash_off_24,
                null
            )
        )
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    private fun setFlashOnIcon() {
        binding.imgFlashLight.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_flash_on_24,
                null
            )
        )
    }

    override fun onQueryCompleted(users: GetDetailsModel?) {
        if(splitted?.size==3){
            val intent = Intent(
                this@RegisterCamera,
                RegisterPatient::class.java
            )
            intent.putExtra("patient_id", splitted!![0])
            intent.putExtra("patient_name", splitted!![1] + " " + splitted!![2])
            startActivity(intent)
            finish()
        }else if(splitted?.size==2){
            val intent = Intent(
                this@RegisterCamera,
                RegisterPatient::class.java
            )
            intent.putExtra("patient_id", splitted!![0])
            intent.putExtra("patient_name", splitted!![1] )
            startActivity(intent)
            finish()
        }else if(splitted?.size==1){
            val intent = Intent(
                this@RegisterCamera,
                RegisterPatient::class.java
            )
            intent.putExtra("patient_id", splitted!![0])
            startActivity(intent)
            finish()
            Toast.makeText(this@RegisterCamera,getString(R.string.no_name),Toast.LENGTH_SHORT).show()

        }else{
            Toast.makeText(this@RegisterCamera,getString(R.string.invalid_qr),Toast.LENGTH_SHORT).show()
        }
    }

    override fun onQueryError(users: GetDetailsModel?) {
        TODO("Not yet implemented")
    }
}
