package com.ramlaxmaninnovation.mds.registration

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import coil.api.load
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.BuildConfig
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.database.UserDatabaseUtils
import com.ramlaxmaninnovation.mds.ml.EmbeddingGenerator
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.ServiceConfig
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import com.ramlaxmaninnovation.mds.verifydevice.VerifyDeviceIntro
import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientDetailsFragment
import kotlinx.android.synthetic.main.patient_details_item.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException

class EditPatientDetails  : AppCompatActivity(){
    lateinit var registerButton: Button
    lateinit var patient_name: EditText
    lateinit var patient_id: EditText
    lateinit var patient_remark: EditText
    lateinit var registerFaceDialogImageInfoTextView: TextView
    lateinit var patient_photo: ImageView
    val bitmap: Bitmap? = null;
    lateinit var bitmapConvertedValue: String
    private val cameraRequest = 1888
    lateinit var photo: Bitmap
    lateinit var TAG: String
    private var registerUserEmbedding = ""
    private lateinit var embeddingGenerator: EmbeddingGenerator
    private lateinit var photoBitmap: Bitmap

    var userPrefManager: UserPrefManager? = null
    private var currentImageTempFile: File? = null

    var splitted:Array<String>?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)
        changeStatusBarColor()
        initView()
        userPrefManager = UserPrefManager(this)
        AppUtils.setLocal(this,userPrefManager!!.language)
        TAG = "EditPatientDetails"

        embeddingGenerator = EmbeddingGenerator(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            request.launch(Manifest.permission.CAMERA)
        }

        val patientId=intent.getStringExtra("patient_id")
        val patientName=intent.getStringExtra("patient_name")
        val patientRemark=intent.getStringExtra("patient_remark")
        val patientPhoto=intent.getStringExtra("patient_photo")


        patient_id.setText(patientId)
        patient_name.setText(patientName)
        patient_remark.setText(patientRemark)
        val decodedString: ByteArray = Base64.decode(patientPhoto, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        Glide.with(this)
            .load(decodedByte)
            .apply(RequestOptions().circleCrop())
            .into(patient_photo)
         photoBitmap=decodedByte
        patient_id.isClickable = false
        patient_id.isFocusable = false;
        patient_photo.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequest)
        }
        showAddFaceDialog()

    }

    private val request = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            showCameraPermissionDenialDialog()
        }
    }

    private fun showCameraPermissionDenialDialog() {
        val alertDialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.camera_permission)
            setMessage(R.string.permission_title)
            setCancelable(false)
            setPositiveButton(R.string.allow) { dialog, which ->
                request.launch(Manifest.permission.CAMERA)
            }
            setNegativeButton(R.string.cancel) { dialog, which ->
                dialog.dismiss()
                finish()
            }
            create()
        }
        alertDialog.show()
    }


    private fun initView() {

        patient_name = findViewById(R.id.patient_name)
        patient_id = findViewById(R.id.patient_id)
        patient_remark = findViewById(R.id.patient_remark)

        registerButton = findViewById(R.id.btRoomRegister)
        registerFaceDialogImageInfoTextView = findViewById(R.id.registerFaceDialogImageInfoTextView)
        patient_photo = findViewById(R.id.patient_photo)
        val photo=intent.getStringExtra("photo")
        if (photo != null) {
            registerUserEmbedding=photo
        }
    }



    fun onBackPress(view: View) {
        val intent = Intent(this@EditPatientDetails, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }



    //toregsiterAppinRoom
    private fun showAddFaceDialog() {

        registerButton.setOnClickListener {
            currentImageTempFile?.delete()
            AppUtils.hideKeyboard( this@EditPatientDetails)
                   getEditPatient(
                        VerifyDeviceIntro.ANDROID_ID(this@EditPatientDetails),
                        patient_id.text.toString(),
                        patient_name.text.toString(),
                        registerUserEmbedding,
                        patient_remark.text.toString(),
                        BitMapToString(photoBitmap))

        }

        patient_photo.setOnClickListener {
            if (patient_name.text!!.isNotBlank() &&
                patient_id.text!!.isNotBlank()
            ) {
                patient_name.error = null
                dispatchTakePictureIntent()
            } else {
                patient_name.error = "Please enter your name here."
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            currentImageTempFile = try {
                File.createTempFile("image", ".jpg", filesDir)
            } catch (ex: IOException) {
                Log.e("APP", ex.message!!)
                null
            }
            currentImageTempFile.also {
                val photoURI =
                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID  + ".provider", it!!)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureResult.launch(takePictureIntent)
            }
        }
    }

    private val takePictureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val picture = BitmapFactory.decodeFile(currentImageTempFile?.absolutePath)

                photoBitmap= getResizedBitmap(picture,200)

                Glide.with(this)
                    .load(picture)
                    .apply(RequestOptions().circleCrop())
                    .into(patient_photo)
//                rotateImage(-90, picture)
//            bitmapConvertedValue= BitMapToString(picture)

                val exifInterface = ExifInterface(currentImageTempFile?.absolutePath!!)
                embeddingGenerator.getFaceEmbedding(
                    picture,
                    exifInterface,
                    embeddingGeneratorCallback
                )
                registerFaceDialogImageInfoTextView.text = getString(R.string.checking)
            }
        }


    fun rotateImage(angle: Int, bitmapSrc: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            bitmapSrc, 0, 0,
            bitmapSrc.width, bitmapSrc.height, matrix, true
        )
    }

    private val embeddingGeneratorCallback = object : EmbeddingGenerator.Callback {

        override fun onEmbeddingGenerated(embedding: String) {
            registerFaceDialogImageInfoTextView.text = getString(R.string.image_selected)
            registerButton.isEnabled = true
            registerUserEmbedding = embedding

        }

        override fun onMultipleFacesFound(numFaces: Int) {
            registerFaceDialogImageInfoTextView.text =
                "Multiple faces were found. Found $numFaces faces."
            registerFaceDialogImageInfoTextView.setTextColor(Color.parseColor("#ff0000"));
        }

        override fun onNoFacesFound() {
            registerFaceDialogImageInfoTextView.text = getString(R.string.no_face_were_found)
            registerFaceDialogImageInfoTextView.setTextColor(Color.parseColor("#ff0000"));
        }

        override fun onError(exception: Exception) {
            Log.e("APP", exception.message!!)
        }

    }

    fun BitMapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun getEditPatient(
        androidId: String,
        id:String,
        name: String,
        photo: String,
        remarks: String,
        photo_string: String
    ) {
        if (AppUtils.isNetworkAvailable(this)) {
            val post = ServiceConfig.createService(RetroOldApi::class.java)
            val call = post.editPatient(androidId,id,name,photo,remarks,photo_string,
                userPrefManager?.location
            )
            call.enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditPatientDetails,getString(R.string.patient_updated),Toast.LENGTH_SHORT).show()
                        val mainActivity = Intent(
                            applicationContext,
                            PatientDetailsFragment::class.java
                        )
                        startActivity(mainActivity)
                        finish()
                    } else if (response.code() == 404) {
                        try {
                            val obj = JSONObject(response.body().toString())
                            val subError =
                                JSONObject(obj.getJSONObject("errors").getString("message"))
                            val userPrefManager = UserPrefManager(this@EditPatientDetails)
                            if (userPrefManager.language.equals("en", ignoreCase = true)) {
                                Toast.makeText(
                                    this@EditPatientDetails,
                                    subError.getString("en"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this@EditPatientDetails,
                                    subError.getString("jpn"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(
                                this@EditPatientDetails,
                                e.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@EditPatientDetails,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        Toast.makeText(this@EditPatientDetails, "socket timeout", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@EditPatientDetails,
                            t.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        } else {

        }
    }
}