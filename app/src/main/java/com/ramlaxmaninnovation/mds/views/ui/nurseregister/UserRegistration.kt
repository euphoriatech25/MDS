package com.ramlaxmaninnovation.mds.views.ui.nurseregister

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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.BuildConfig
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.database.UserDatabaseUtils
import com.ramlaxmaninnovation.mds.ml.EmbeddingGenerator
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.ServiceConfig
import com.ramlaxmaninnovation.mds.registration.RegisterResponse
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.views.ui.nurselist.NurseListViewFragment
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException

class UserRegistration : AppCompatActivity() {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        changeStatusBarColor()
        initView()
        userPrefManager = UserPrefManager(this)
        AppUtils.setLocal(this, userPrefManager!!.language)
        TAG = "UserRegistration"

        embeddingGenerator = EmbeddingGenerator(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            request.launch(Manifest.permission.CAMERA)
        }

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
//        textInputFirstName.visibility=View.GONE
    }


    fun onBackPress(view: View) {
        val intent = Intent(this@UserRegistration, MainActivity::class.java)
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
            if (registerUserEmbedding.isNotEmpty() && registerUserEmbedding != "") {
                currentImageTempFile?.delete()
                var a = userPrefManager?.id?.plus(1)
                userPrefManager?.let { it1 ->
                    UserDatabaseUtils.insertUser(
                        this,
                        patient_name.text.toString(),
                        patient_name.text.toString(),
                        registerUserEmbedding,
                        patient_remark.text.toString(),
                        BitMapToString(photoBitmap),
                        it1.terminalName
                    )
                    if (a != null) {
                        userPrefManager!!.id = a
                    }
                    Toast.makeText(this, getString(R.string.face_registered), Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this@UserRegistration, NurseListViewFragment::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, getString(R.string.photo_error), Toast.LENGTH_LONG).show()
            }
        }


        patient_photo.setOnClickListener {
            if (patient_name.text!!.isNotBlank()
//                patient_id.text!!.isNotBlank()
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
                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", it!!)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureResult.launch(takePictureIntent)
            }
        }
    }

    private val takePictureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val picture = BitmapFactory.decodeFile(currentImageTempFile?.absolutePath)

                photoBitmap = getResizedBitmap(picture, 200)

                Glide.with(this)
                    .load(picture)
                    .apply(RequestOptions().circleCrop())
                    .into(patient_photo)


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

    }
