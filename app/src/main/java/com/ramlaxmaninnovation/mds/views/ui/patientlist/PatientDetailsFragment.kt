package com.ramlaxmaninnovation.mds.views.ui.patientlist

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opencsv.CSVWriter
import com.ramlaxmaninnovation.App
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.FragmentPatientDetailsBinding
import com.ramlaxmaninnovation.mds.network.RetroOldApi
import com.ramlaxmaninnovation.mds.network.ServiceConfig
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.nio.file.Files
import java.util.ArrayList


class PatientDetailsFragment : AppCompatActivity() {

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

        binding.onBckPress.setOnClickListener {  val intent = Intent(this, MainActivity::class.java).apply {
            intent.putExtra("from","Register")
        }
            startActivity(intent) }
        userPrefManager = UserPrefManager(this)
        setupViewModel()

    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    private fun init() {

        productAdapter = PatientAdapter()
        layoutManager = GridLayoutManager(this, 1)
        binding.patientDetailsRecyclerView.layoutManager = layoutManager
        binding.patientDetailsRecyclerView.setHasFixedSize(true)
        binding.patientDetailsRecyclerView.isFocusable = false
        binding.patientDetailsRecyclerView.adapter = productAdapter


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
                        productAdapter.differ.submitList(productModel.data)
                        binding.patientDetailsRecyclerView.adapter = productAdapter

                         if(productModel.data.isNotEmpty()){
                            binding.convertTableToPdf.setOnClickListener { verifyStoragePermissions(this) }
                         }
//                        getPatientView(productModel.data)
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
//        da
//        val itemView: View = LayoutInflater.from(this@NurseListViewFragment)
//            .inflate(R.layout.user_item, binding.availableNurseList, false)
//
//        var user_id: TextView = itemView.findViewById(R.id.user_id)
//        var user_name: TextView = itemView.findViewById(R.id.user_name)
//        var user_photo: ImageView = itemView.findViewById(R.id.user_photo)
//        var user_remarks: TextView = itemView.findViewById(R.id.user_remark)
//        var terminal_used: TextView = itemView.findViewById(R.id.terminal_name)
//        var loginNurse: TextView = itemView.findViewById(R.id.login_nurse)


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

    fun verifyStoragePermissions(activity: Activity?) {
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
                        userPrefManager?.terminalName+ "\n"
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