package com.ramlaxmaninnovation.mds.camera


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings.Secure
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.cardview.widget.CardView
import androidx.core.graphics.toRectF
import com.app.ramlaxmangroup.face_recognition.ml.FaceNetModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.ServiceConfig
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt
import android.app.Activity
import android.view.View


class FrameAnalyser(
    private var context: Context,
    private var boundingBoxOverlay: BoundingBoxOverlay,
    private var userEmbeddings: Array<FloatArray>,
    private var userName: String,
    private var idFace: String,
    private var reMarks: String,
    private var statusDisplay: CardView,
    private var patient_details: TextView,
    private var status_face_verification: TextView,
    private var continue_verification: TextView,
    private var source: String,
    private var photo: String,
    private var show: TextView,
) : ImageAnalysis.Analyzer {
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val detector = FaceDetection.getClient(realTimeOpts)
    private val model = FaceNetModel(context)
    var classificationThreshold = 0.9f
    private var isProcessing = false
    private var requireSend = true

    private var a = 0
    private var b = 0
    private var c = 0

    override fun analyze(image: ImageProxy) {
        val frameBitmap = BitmapUtils.rotateBitmap(
            BitmapUtils.imageToBitmap(image.image!!),
            image.imageInfo.rotationDegrees.toFloat()
        )

        if (!boundingBoxOverlay.areDimsInit) {
            boundingBoxOverlay.frameHeight = frameBitmap.height
            boundingBoxOverlay.frameWidth = frameBitmap.width
        }

        if (isProcessing) {
            image.close()
            return
        } else {
            isProcessing = true
            val inputImage = InputImage.fromMediaImage(image.image, image.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    CoroutineScope(Dispatchers.Main).launch {
                        runModel(faces, frameBitmap)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("App", e.message!!)
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun runModel(faces: List<Face>, cameraFrameBitmap: Bitmap) {
        withContext(Dispatchers.Default) {
            val overlayBoundingBoxes = ArrayList<RectF>()
            val overlayNames = ArrayList<String>()
            for (face in faces) {
                try {
                    val subject = model.getFaceEmbedding(
                        BitmapUtils.cropBitmap(
                            cameraFrameBitmap,
                            face.boundingBox
                        )
                    )
                    overlayBoundingBoxes.add(face.boundingBox.toRectF())
                    val scores = FloatArray(userEmbeddings.size)
                    for (i in userEmbeddings.indices) {
                        scores[i] = L2Norm(subject, userEmbeddings[i])
                    }


                    val result = scores.map {
                        if (it > classificationThreshold) {
                            -1
                        } else {
                            1
                        }
                    }.sum()
                    if (!scores.average().toFloat().toString().equals("NaN")) {
                        if (result >= 0 && scores.average().toFloat() <= classificationThreshold) {

                            if(source=="verify"){
                                show.visibility= View.GONE
                                patient_details.text =
                                    context.getString(R.string.VERIFYING_FOR)+"\n" + userName
                                continue_verification.text =
                                    context.getString(R.string.patient_id) + " " + idFace
                            }else{
                                show.visibility= View.GONE
                                patient_details.text =
                                   context.getString(R.string.verifying_user)+" :-\n" +userName
                                continue_verification.text =
                                    context.getString(R.string.user_id) + "  " + idFace
                            }


                            overlayNames.add(userName + " ${scores.average().toFloat()}")

                            a++
                            c = 0
                            if (a == 2) {
                                status_face_verification.text =
                                    context.getString(R.string.verified_customer)
                                statusDisplay.setCardBackgroundColor(Color.GREEN)

                                if (source == "verify"){
                                    if (registerTransactionDetails(context, idFace, userName)) {
                                        requireSend = false
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.internet_connection),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    soundEffectForSuccessfullValidation(context)
                                }else if (source=="user"){
                                    soundEffectForSuccessfullValidation(context)
                                    val userPrefManager = UserPrefManager(context)
                                    userPrefManager.setNurseDetails(idFace,userName,photo)
                                }
                            }
                        } else if (result < 0) {
                            overlayNames.add(
                                context.getString(R.string.unknown) + " ${
                                    scores.average().toFloat()
                                }"
                            )
                            a = 0
                            c++
                            if (c == 5) {
                                soundEffectForErrorValidation(context)
                                status_face_verification.text =
                                    context.getString(R.string.patient_not_found)
                                statusDisplay.setCardBackgroundColor(Color.RED)
                                patient_details.setTextColor(Color.WHITE)
                                continue_verification.setTextColor(Color.WHITE)
                                status_face_verification.setTextColor(Color.WHITE)
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("Model", "Exception in FrameAnalyser : ${e.message}")
                    continue
                }
            }
            withContext(Dispatchers.Main) {
                boundingBoxOverlay.faceBoundingBoxes = overlayBoundingBoxes
                boundingBoxOverlay.faceNames = overlayNames
                boundingBoxOverlay.invalidate()
                isProcessing = false
            }
        }
    }

    private fun soundEffectForErrorValidation(context: Context) {

        try {
            val mediaPlayer = MediaPlayer.create(
                context, R.raw.wrong
            )
            mediaPlayer.start()
            val v = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)!!
            // Vibrate for 500 milliseconds
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v!!.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                //deprecated in API 26
                v!!.vibrate(500)
            }
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }

    private fun soundEffectForSuccessfullValidation(ctx: Context) {

        try {
            val mediaPlayer = MediaPlayer.create(
                ctx, R.raw.correct
            )
            mediaPlayer.start()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }


    private fun L2Norm(x1: FloatArray, x2: FloatArray): Float {
        var sum = 0.0f
        val mag1 = sqrt(x1.map { xi -> xi.pow(2) }.sum())
        val mag2 = sqrt(x2.map { xi -> xi.pow(2) }.sum())
        for (i in x1.indices) {
            sum += ((x1[i] / mag1) - (x2[i] / mag2)).pow(2)
        }
        return sqrt(sum)
    }


    private fun registerTransactionDetails(context: Context, idFace: String?, userName: String): Boolean {
        if (requireSend) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val calendar = Calendar.getInstance()
            val date = dateFormat.format(calendar.time)
            var userPrefManager = UserPrefManager(context)

            if (AppUtils.isNetworkAvailable(context)) {
                Log.i("TAG", "registerTransactionDetails: ")
                val post = ServiceConfig.createService(RetroOldApi::class.java)
                val call = post.addTransaction(idFace, ANDROID_ID(context), date,
                    userPrefManager.nurseDetails[1],userPrefManager.terminalName
                )

                call.enqueue(object : Callback<ResponseBody?> {
                    override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                        if (response.isSuccessful) {
                            showDialog(context,userName,true)
                        } else if (response.code() == 400) {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                Toast.makeText(
                                    context,
                                    jObjError.getJSONObject("error").getString("message"),
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                            }
                        }else if(response.code()==500){
                            Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                        Log.i("TAG", "onFailure: " + t.localizedMessage)
                        if (t is SocketTimeoutException) {
                        } else {
                        }
                    }
                })
                return true
            } else {
                Toast.makeText(context, context.getString(R.string.no_interest_connection), Toast.LENGTH_LONG).show()
                return false
            }
        }else{
           return requireSend
        }
        return false
    }

    @SuppressLint("HardwareIds")
    fun ANDROID_ID(context: Context): String? {
        return Secure.getString(
            context.contentResolver,
            Secure.ANDROID_ID
        ).toUpperCase()
    }

    fun showDialog(activity: Context, msg: String?, b: Boolean) {
        if (!(context as Activity).isFinishing) {
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.pop_up_face_recognization)
            val verification_result = dialog.findViewById(R.id.verification_result) as TextView
            val response_background = dialog.findViewById(R.id.response_background) as CardView
            verification_result.text = msg

            val cancel = dialog.findViewById(R.id.cancel) as ImageView



            cancel.setOnClickListener {
                val intent = Intent(context, CameraViewActivity::class.java).apply {
                }
                context.startActivity(intent)
                dialog.dismiss()
            }


            if (b) {
                verification_result.setText(context.getString(R.string.patient_verified) + "\n" + userName)
                response_background.setCardBackgroundColor(Color.GREEN)
                verification_result.setTextColor(Color.GREEN)
            } else {
//            verification_result.setText(context.getString(R.string.unknown))
//            response_background.setCardBackgroundColor(Color.RED)
//            verification_result.setTextColor(Color.RED)
            }


            val continueBtn = dialog.findViewById(R.id.continueBtn) as Button
            continueBtn.setOnClickListener {
                val intent = Intent(context, CameraViewActivity::class.java).apply {
                }
                context.startActivity(intent)
                dialog.dismiss()
            }

            dialog.show()
          }
        }


}
