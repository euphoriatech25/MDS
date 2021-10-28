package com.ramlaxmaninnovation.mds.views.ui.transactionlist

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.opencsv.CSVWriter
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.FragmentTransactionListBinding
import com.ramlaxmaninnovation.mds.repository.AppRepository
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.utils.subUtils.Resource
import com.ramlaxmaninnovation.mds.utils.subUtils.errorSnack
import com.ramlaxmaninnovation.mds.viewModel.TransactionViewModel
import com.ramlaxmaninnovation.mds.viewModel.ViewModelProviderFactory
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.text.DateFormat
import kotlin.collections.ArrayList


class TransactionListFragment : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    lateinit var productAdapter: TransactionAdapter
    var calStarting = Calendar.getInstance()
    var calEnding = Calendar.getInstance()

    var calStartingDisplay = Calendar.getInstance()
    var calEndingDisplay = Calendar.getInstance()


    var userPrefManager: UserPrefManager? = null
    private val dataArrayList = ArrayList<Data>()
    private lateinit var transactionModel: TransactionModel
    private val TAG = "TransactionListFragment"

    private var _binding: FragmentTransactionListBinding? = null

    private var layoutManager: GridLayoutManager? = null

    private lateinit var binding: FragmentTransactionListBinding

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var startingDate: String = ""
    var endingDate: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor()

        binding.onBckPress.setOnClickListener {  val intent = Intent(this, MainActivity::class.java).apply {

        }
            startActivity(intent) }
        userPrefManager = UserPrefManager(this)
        init()

    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    private fun init() {

        productAdapter = TransactionAdapter()
        layoutManager = GridLayoutManager(this, 1)
        binding.transactionRecyclerView.layoutManager = layoutManager
        binding.transactionRecyclerView.setHasFixedSize(true)
        binding.transactionRecyclerView.isFocusable = false
        binding.transactionRecyclerView.adapter = productAdapter


        setupViewModel()
        getSearchDATES()

        binding.searchBetweenTransaction.setOnClickListener {
            if(transactionModel.data.isNotEmpty())
            if (startingDate.isNotEmpty() && endingDate.isNotEmpty()) {
                dataArrayList.clear()
                val format: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                Log.i(TAG, "init: $startingDate $endingDate")

                val datestart: Date = format.parse(startingDate)
                val dateend: Date = format.parse(endingDate)
             for(i in  transactionModel.data){
                 var date:String=i.last_consumption_date.substring(0, i.last_consumption_date.length - 9)
                 val total: Date = format.parse(date)
                 if(total.after(datestart)&&total.before(dateend)){
                     dataArrayList.add(i)
                     Log.i(TAG, "init: "+i.last_consumption_date+" ")
                 }

                 productAdapter.differ.submitList(dataArrayList)
                 binding.transactionRecyclerView.adapter = productAdapter
                 productAdapter.notifyDataSetChanged()

             }


            } else {
                Toast.makeText(
                    this@TransactionListFragment,
                    "Please select Starting/Ending Date",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getSearchDATES() {

        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(
                view: DatePicker, year: Int, monthOfYear: Int,
                dayOfMonth: Int
            ) {
                calStarting.set(Calendar.YEAR, year)
                calStarting.set(Calendar.MONTH, monthOfYear)
                calStarting.set(Calendar.DAY_OF_MONTH, dayOfMonth-1)


                calStartingDisplay.set(Calendar.YEAR, year)
                calStartingDisplay.set(Calendar.MONTH, monthOfYear)
                calStartingDisplay.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInViewStarting(calStartingDisplay)
            }
        }

        val dateSetListenersEnding = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(
                view: DatePicker, year: Int, monthOfYear: Int,
                dayOfMonth: Int
            ) {
                calEnding.set(Calendar.YEAR, year)
                calEnding.set(Calendar.MONTH, monthOfYear)
                calEnding.set(Calendar.DAY_OF_MONTH, dayOfMonth+1)

                calEndingDisplay.set(Calendar.YEAR, year)
                calEndingDisplay.set(Calendar.MONTH, monthOfYear)
                calEndingDisplay.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateInViewEnding(calEndingDisplay)
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        binding.startingDate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(
                    this@TransactionListFragment,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    calStarting.get(Calendar.YEAR),
                    calStarting.get(Calendar.MONTH),
                    calStarting.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

        })

        binding.endingDate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(
                    this@TransactionListFragment,
                    dateSetListenersEnding,
                    // set DatePickerDialog to point to today's date when it loads up
                    calEnding.get(Calendar.YEAR),
                    calEnding.get(Calendar.MONTH),
                    calEnding.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        })

    }


    private fun updateDateInViewStarting(calStartingDisplay: Calendar) {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.startingDate.text = sdf.format(calStartingDisplay.getTime())
        startingDate = sdf.format(calStarting.getTime())
    }

    private fun updateDateInViewEnding(calEndingDisplay: Calendar) {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.endingDate.text = sdf.format(calEndingDisplay.getTime())
        endingDate = sdf.format(calEnding.getTime())

    }

    private fun setupViewModel() {
        Log.i("TAG", "onCreateView: i reached here 2")

        val repository = AppRepository()
        val factory = ViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, factory).get(TransactionViewModel::class.java)
        getPictures()
    }

    private fun getPictures() {
        userPrefManager?.let { viewModel.getTransactionById(it?.terminalName) }
        viewModel.transactionDetailsById.observe(this, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    response.data?.let { picsResponse ->
                        transactionModel = response.data
                        Log.i("TAG", "getPictures: " + transactionModel.data[0].device_name)
                        productAdapter.differ.submitList(transactionModel.data)
                        binding.transactionRecyclerView.adapter = productAdapter
                        binding.getReprot.setOnClickListener {
                            verifyStoragePermissions(this)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        binding.transactionRecyclerView.errorSnack(message, Snackbar.LENGTH_LONG)
                    }

                }

                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        progress.visibility = View.GONE
    }

    private fun showProgressBar() {
        progress.visibility = View.VISIBLE
    }


    fun onProgressClick(view: View) {
        //Preventing Click during loading
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
                val filename = "transaction_report.csv"
                val baseDir = Environment.getExternalStorageDirectory().absolutePath
                val filePath = baseDir + File.separator + filename
                Log.i(
                    TAG,
                    "verifyStoragePermissions: $filePath"
                )
                val filelocation = File(filePath)
                val path = FileProvider.getUriForFile(
                    this@TransactionListFragment,
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
                for (i in transactionModel.data.indices) {
                    data = arrayOf(
                        transactionModel.data[i].toString(),
                        transactionModel.data[i].patient_id,
                        transactionModel.data[i].name,
                        transactionModel.data[i].last_consumption_date,
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

}