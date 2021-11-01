package com.ramlaxmaninnovation.mds.views.ui.patientlist

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.opencsv.CSVWriter
import com.ramlaxmaninnovation.App
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.FragmentPatientDetailsBinding
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.ServiceConfig
import com.ramlaxmaninnovation.mds.registration.EditPatientDetails
import com.ramlaxmaninnovation.mds.registration.RegisterCamera
import com.ramlaxmaninnovation.mds.registration.RegisterPatient
import com.ramlaxmaninnovation.mds.repository.AppRepository
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.ErrorMsg
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.utils.subUtils.Resource
import com.ramlaxmaninnovation.mds.utils.subUtils.errorSnack
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import com.ramlaxmaninnovation.mds.viewModel.PatientListViewModel
import com.ramlaxmaninnovation.mds.viewModel.ViewModelProviderFactory
import kotlinx.android.synthetic.main.patient_details_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.nio.file.Files
import java.util.ArrayList
import android.content.DialogInterface





class PatientDetailsFragment :  AppCompatActivity() {

    private val TAG = "PatientDetailsFragment"
    private var _binding: FragmentPatientDetailsBinding? = null
    private lateinit var viewModel: PatientListViewModel
    lateinit var productAdapter: PatientAdapter
    private lateinit var productModel: PatientListModel
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var layoutManager: GridLayoutManager? = null

    private lateinit var binding: FragmentPatientDetailsBinding
    var userPrefManager: UserPrefManager? = null

    //    override fun onCreateView(
//        inflater: LayoutInflater,
//        parent: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentPatientDetailsBinding.inflate(layoutInflater, parent, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor()

        binding.onBckPress.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                intent.putExtra("from", "Register")
            }
            startActivity(intent)
        }
        userPrefManager = UserPrefManager(this)
        setupViewModel()

    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    private fun init() {

//        productAdapter = PatientAdapter()
//        layoutManager = GridLayoutManager(this, 1)
//        binding.patientDetailsRecyclerView.layoutManager = layoutManager
//        binding.patientDetailsRecyclerView.setHasFixedSize(true)
//        binding.patientDetailsRecyclerView.isFocusable = false
//        binding.patientDetailsRecyclerView.adapter = productAdapter


        binding.signupUser.setOnClickListener {
//            val intent = Intent(this, RegisterPatient::class.java)
////            intent.putExtra(PRODUCT_ID,categoriesItem.id.toString())
//            startActivity(intent)
            val intent = Intent(this, RegisterCamera::class.java).apply {

            }
            startActivity(intent)
        }

    }


    private fun setupViewModel() {
        Log.i("TAG", "onCreateView: i reached here 2")

        val repository = AppRepository()
        val factory = ViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, factory).get(PatientListViewModel::class.java)


        getPictures()
        init()
    }

    private fun getPictures() {
        userPrefManager?.terminalName?.let { viewModel.getProductById(it) }

        viewModel.productDetailsById.observe(this, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    response.data?.let { picsResponse ->
                        productModel = picsResponse
//                        productAdapter.differ.submitList(productModel.data)
//                        binding.patientDetailsRecyclerView.adapter = productAdapter

                        if (productModel.data.isNotEmpty()) {
                            binding.convertTableToPdf.setOnClickListener {
                                verifyStoragePermissions(
                                    this
                                )
                            }
                        }
                        viewPatientDetails(productModel.data)
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        binding.patientDetailsRecyclerView.errorSnack(message, Snackbar.LENGTH_LONG)
                    }
                }
                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun viewPatientDetails(data: List<Data>) {

        var imageBitmap:String
        if(data.isNotEmpty()){
            for(i in data.indices){
                val itemView: View = LayoutInflater.from(this@PatientDetailsFragment)
                    .inflate(R.layout.patient_details_item, binding.patientDetailsRecyclerView, false)

                var patient_id: TextView = itemView.findViewById(R.id.patient_id)
                var patient_name: TextView = itemView.findViewById(R.id.patient_name)
                var patient_photo: ImageView = itemView.findViewById(R.id.patient_photo)
                var patient_comment: TextView = itemView.findViewById(R.id.patient_comment)
                var terminal_used: TextView = itemView.findViewById(R.id.terminal_used)
                var patient_no: TextView = itemView.findViewById(R.id.patient_no)
                var edit_patient: ImageButton = itemView.findViewById(R.id.edit_patient)
                var delete_patient: ImageButton = itemView.findViewById(R.id.delete_patient)

                patient_no.text = (i+1).toString()
                patient_id.text = data[i].patient_id
                patient_name.text = data[i].name
                patient_comment.text=data[i].remarks
                terminal_used.text = data[i].device_name
                imageBitmap = data[i].photo_string

                val decodedString: ByteArray = Base64.decode(imageBitmap, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                patient_photo.load(decodedByte)
                binding.patientDetailsRecyclerView.addView(itemView)


                edit_patient.setOnClickListener {
                    Log.i("TAG", "onBindViewHolder: i m here")
                    val intent = Intent( App.getContext(), EditPatientDetails::class.java)

                    intent.putExtra("patient_id", data[i].patient_id)

                    intent.putExtra("patient_name",data[i].name)

                    intent.putExtra("patient_remark",data[i].remarks)

                    intent.putExtra("patient_photo",data[i].photo_string)

                    intent.putExtra("photo",data[i].photo)

                    startActivity(intent)
                }
                delete_patient.setOnClickListener {

                    val dialogClickListener =
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {

                                    deleteDevice(data[i].patient_id,
                                        AppUtils.ANDROID_ID(App.getContext())
                                    )
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    dialog.dismiss()
                                }
                            }
                        }

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setMessage(getString(R.string.delete_patient))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show()

                }

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
                        val intent = Intent( this@PatientDetailsFragment,PatientDetailsFragment::class.java)
                        startActivity(intent)
                    } else if (response.code() == 404) {
                        AppUtils.convertErrors(response.errorBody())
                    } else {
                        Toast.makeText(
                            this@PatientDetailsFragment,
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



    private fun hideProgressBar() {
        binding.progress.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progress.visibility = View.VISIBLE
    }


    fun onProgressClick(view: View) {
        //Preventing Click during loading
    }

    private fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        } else {
            if (writeDataAtOnce()) {
                val filename = "patient_list_report.csv"
                val baseDir = Environment.getExternalStorageDirectory().absolutePath
                val filePath = baseDir + File.separator + filename
                Log.i(
                    TAG,
                    "verifyStoragePermissions: $filePath"
                )
                val filelocation = File(filePath)
                val path = FileProvider.getUriForFile(
                    this@PatientDetailsFragment,
                    applicationContext.packageName.toString() + ".provider",
                    filelocation
                )
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.type = "vnd.android.cursor.dir/email"
                val to = arrayOf("")
                emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
                emailIntent.putExtra(Intent.EXTRA_STREAM, path)
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
                startActivity(Intent.createChooser(emailIntent, "Send email..."))
            }
        }
    }

    fun writeDataAtOnce(): Boolean {
        val mFileWriter: FileWriter
        val baseDir = Environment.getExternalStorageDirectory().absolutePath
        val fileName = "patient_list_report.csv"
        val filePath = baseDir + File.separator + fileName
        val f = File(filePath)
        val writer: CSVWriter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // File exist
                Log.i("TAG", "writeDataAtOnce:no issue ")
                if (f.exists() && !f.isDirectory) {
                    val result = Files.deleteIfExists(f.toPath())
                    Log.e(
                        TAG,
                        "writeDataAtOnce: $result"
                    )
                    mFileWriter = FileWriter(filePath)
                    writer = CSVWriter(mFileWriter)

                } else {
                    writer = CSVWriter(FileWriter(filePath))
                }

                var data: Array<String>? = null
                for (i in productModel.data.indices) {
                    data = arrayOf(
                        productModel.data[i].toString(),
                        productModel.data[i].patient_id,
                        productModel.data[i].name,
                        productModel.data[i].remarks,
                        userPrefManager?.terminalName + "\n"
                    )
                    Log.i(
                        TAG,
                        "writeDataAtOnce: " + data.size
                    )
                    writer.writeNext(data)
                }
                writer.close()
            } catch (e: Exception) {
                Log.i("TAG", "writeDataAtOnce: " + e.localizedMessage)
            }
        }
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}