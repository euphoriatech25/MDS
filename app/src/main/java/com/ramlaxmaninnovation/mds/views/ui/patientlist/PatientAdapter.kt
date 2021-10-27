package com.ramlaxmaninnovation.mds.views.ui.patientlist

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.ramlaxmaninnovation.App
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.ServiceConfig
import com.ramlaxmaninnovation.mds.registration.EditPatientDetails
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.AppUtils.ANDROID_ID
import com.ramlaxmaninnovation.mds.utils.ErrorMsg
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.utils.subUtils.Resource
import com.ramlaxmaninnovation.mds.utils.subUtils.errorSnack
import com.ramlaxmaninnovation.mds.verifydevice.VerifyDeviceIntro
import com.ramlaxmaninnovation.mds.viewModel.PatientListViewModel
import kotlinx.android.synthetic.main.patient_details_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Observer


class PatientAdapter() : RecyclerView.Adapter<PatientAdapter.ProductListViewHolder>() {
    var userPrefManager: UserPrefManager? = null
      var context:Context?=null
    inner class ProductListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    lateinit var imageBitmap: String
    private val differCallback = object : DiffUtil.ItemCallback<Data>() {

        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.patient_id == newItem.patient_id
            Log.i("TAG", "areItemsTheSame: " + oldItem.patient_id)
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductListViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.patient_details_item,
            parent,
            false
        )
    )

    override fun getItemCount() = differ.currentList.size


    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val patientItem = differ.currentList[position]
        userPrefManager = UserPrefManager(App.getContext())

          this.context=context

        holder.itemView.apply {
            patient_no.text = position.toString()
            patient_id.text = patientItem.patient_id
            patient_name.text = patientItem.name
            patient_comment.text=patientItem.remarks
            terminal_used.text = patientItem.device_name
            imageBitmap = patientItem.photo_string

            val decodedString: ByteArray = Base64.decode(imageBitmap, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            patient_photo.load(decodedByte)

            edit_patient.setOnClickListener {
                Log.i("TAG", "onBindViewHolder: i m here")
                val intent = Intent( App.getContext(),EditPatientDetails::class.java)

                intent.putExtra("patient_id", patientItem.patient_id)

                intent.putExtra("patient_name",patientItem.name)

                intent.putExtra("patient_remark",patientItem.remarks)

                intent.putExtra("patient_photo",patientItem.photo_string)

                intent.putExtra("photo",patientItem.photo)

                context.startActivity(intent)
            }
            delete_patient.setOnClickListener {
                deleteDevice(patientItem.patient_id,
                    ANDROID_ID(App.getContext()))
            }

        }
    }


    private fun deleteDevice(patient_id: String, android_id: String) {
        if (AppUtils.isNetworkAvailable( App.getContext())) {
            val post = ServiceConfig.createService(RetroOldApi::class.java)
            val call = post.deletePatient(userPrefManager?.location,patient_id)
            Log.i("TAG", "deleteDevice: "+call.request())
            call.enqueue(object : Callback<ErrorMsg?> {
                override fun onResponse(call: Call<ErrorMsg?>, response: Response<ErrorMsg?>) {
                    if (response.isSuccessful) {
                        val intent = Intent( App.getContext(),PatientDetailsFragment::class.java)
                        context?.startActivity(intent)
                    } else if (response.code() == 404) {
                        AppUtils.convertErrors(response.errorBody())
                    } else {
                        Toast.makeText(
                            context,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ErrorMsg?>, t: Throwable) {
                    Log.i("TAG", "onFailure: " + t.localizedMessage)
                    Toast.makeText( App.getContext(), t.localizedMessage, Toast.LENGTH_SHORT)
                        .show()

                }
            })
        } else {

            Toast.makeText(
                App.getContext(),
                App.getContext().getString(R.string.no_interest_connection),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}
