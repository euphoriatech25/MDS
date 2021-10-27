package com.ramlaxmaninnovation.mds.getPatientDetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.RetrofitInterface
import com.ramlaxmaninnovation.mds.network.ServiceConfig
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.net.SocketTimeoutException

class GetDetails {

    interface CallbackOnline {
        fun onQueryCompleted(users: GetDetailsModel.Data?)
        fun onQueryError(users: GetDetailsModel.Error?)
    }

    companion object {

        fun getUserDetails(context: Context, faceID: String) {

            var callbackOnline: CallbackOnline
            val data: GetDetailsModel.Data? = null
            val post = ServiceConfig.createService(
                RetroOldApi::class.java
            )
            var  userPrefManager:UserPrefManager
            userPrefManager= UserPrefManager(context)
            val call = post.fetchData(faceID,userPrefManager.location)
            call.enqueue(object : retrofit2.Callback<GetDetailsModel?> {
                override fun onResponse(
                    call: Call<GetDetailsModel?>,
                    response: Response<GetDetailsModel?>
                ) {
                    if (response.isSuccessful) {
                        var patient = response.body()
                        callbackOnline = context as CallbackOnline
                        if (patient != null) {
                            callbackOnline.onQueryCompleted(patient.getData())
                        }

                    } else if (response.code() == 404) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val subError = JSONObject(jObjError.getJSONObject("error").getString("message"))
                            val userPrefManager = UserPrefManager(context)

                            if (userPrefManager.language.equals("en", ignoreCase = true)) {
                                showDialog(context, subError.getString("en"),false)
                            } else {
                                showDialog(context, subError.getString("jpn"),false)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                        }
                    }else if(response.code()==500){
                        showDialog(context, context.getString(R.string.internal_server_error),false)
                    }else{
                        showDialog(context,response.message(),false)
                    }
                }

                override fun onFailure(call: Call<GetDetailsModel?>, t: Throwable) {
                    Log.i("TAG", "onFailure" + t.localizedMessage)
                    showDialog(context,t.localizedMessage,false)
                    if (t is SocketTimeoutException) {
//                        listener.connectionTimeOut()
                    } else {
//                        listener.unKnownError()
                    }
                }
            })
        }

        fun showDialog(activity: Context, msg: String, b: Boolean) {
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.error_dialog)
            val verification_result = dialog.findViewById(R.id.msg) as TextView
            val error_bt = dialog.findViewById(R.id.error_btn) as Button
            verification_result.text = msg
            error_bt.setOnClickListener(View.OnClickListener {
                val intent = Intent(activity, CameraViewActivity::class.java).apply {
                }
                activity.startActivity(intent)
            })
            dialog.show()
        }
    }
}