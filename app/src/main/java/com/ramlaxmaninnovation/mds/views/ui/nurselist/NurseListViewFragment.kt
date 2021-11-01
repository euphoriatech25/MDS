package com.ramlaxmaninnovation.mds.views.ui.nurselist

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.opencsv.CSVWriter
import com.ramlaxmaninnovation.App
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.database.User
import com.ramlaxmaninnovation.mds.database.UserDatabaseUtils
import com.ramlaxmaninnovation.mds.databinding.FragmentNurseListViewBinding
import com.ramlaxmaninnovation.mds.utils.AppUtils
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.views.ui.nurseregister.UserRegistration
import java.io.File
import java.io.FileWriter
import java.nio.file.Files


class NurseListViewFragment : AppCompatActivity() {
    private val TAG = "NurseListViewFragment"

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var userPrefManager: UserPrefManager
    private lateinit var binding: FragmentNurseListViewBinding
    private lateinit var userList: Array<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentNurseListViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.onBckPress.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
            }
            startActivity(intent)
        }
        userPrefManager = UserPrefManager(this)
        UserDatabaseUtils.getUsersFromFace(this, databaseCallback)
        userPrefManager = UserPrefManager(this)
        changeStatusBarColor()
        registerNurse()


    }

    fun onBackPress(view: View) {
        val intent = Intent(this@NurseListViewFragment, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    private val databaseCallback = object : UserDatabaseUtils.Companion.Callback {
        override fun onQueryCompleted(users: Array<User>) {
//            val embeddings = users.map{ EmbeddingUtils.stringToFloatArray( it.embedding!! , 128 ) }.toTypedArray()
//            val name = users[ 0 ].personName!!
//            val id=users[0].faceID!!
//            val remarks= users[0].patientRemarks

            if (users.isNotEmpty()) {
                userList = users
                for (i in users) {
                    val itemView: View = LayoutInflater.from(this@NurseListViewFragment)
                        .inflate(R.layout.user_item, binding.availableNurseList, false)

                    var user_id: TextView = itemView.findViewById(R.id.user_id)
                    var user_name: TextView = itemView.findViewById(R.id.user_name)
                    var user_photo: ImageView = itemView.findViewById(R.id.user_photo)
                    var user_remarks: TextView = itemView.findViewById(R.id.user_remark)
                    var terminal_used: TextView = itemView.findViewById(R.id.terminal_name)
                    var loginNurse: TextView = itemView.findViewById(R.id.login_nurse)

                    var deleteNurse: ImageButton = itemView.findViewById(R.id.delete_nurse)

                    user_id.text=i.faceID
                    user_name.text = i.personName
                    user_id.text = i.id.toString()
//                    user_remarks.text = i.patientRemarks
                    terminal_used.text = i.terminalName

                    val decodedString: ByteArray = Base64.decode(i.photostring, Base64.DEFAULT)
                    val decodedByte =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    Glide.with(this@NurseListViewFragment)
                        .load(decodedByte)
                        .apply(RequestOptions().circleCrop())
                        .into(user_photo)

                    loginNurse.setOnClickListener {
                        val intent =
                            Intent(this@NurseListViewFragment, FaceVerificationActivity::class.java)
                        Log.i(TAG, "onQueryCompleted: " + i.id.toString())
                        intent.putExtra("faceID", i.faceID.toString())
                        startActivity(intent)
                    }
                    binding.getReport.setOnClickListener { verifyStoragePermissions(this@NurseListViewFragment) }


                   deleteNurse.setOnClickListener {



                       val dialogClickListener =
                           DialogInterface.OnClickListener { dialog, which ->
                               when (which) {
                                   DialogInterface.BUTTON_POSITIVE -> {

                                       UserDatabaseUtils.deleteUsersFromFaceID(
                                           this@NurseListViewFragment,
                                           i.faceID
                                       )
                                       Toast.makeText(this@NurseListViewFragment,getString(R.string.success_nurse_delete),Toast.LENGTH_SHORT).show()
                                       val intent = Intent(this@NurseListViewFragment, NurseListViewFragment::class.java)
                                       startActivity(intent)
                                       finish()

                                   }
                                   DialogInterface.BUTTON_NEGATIVE -> {
                                       dialog.dismiss()
                                   }
                               }
                           }

                       val builder: AlertDialog.Builder = AlertDialog.Builder(this@NurseListViewFragment)
                       builder.setMessage(getString(R.string.delete_user))
                           .setPositiveButton(getString(R.string.yes), dialogClickListener)
                           .setNegativeButton(getString(R.string.no), dialogClickListener).show()

                   }

                    binding.availableNurseList.addView(itemView);

                }
            } else {
                userPrefManager.setNurseDetails("NOT FOUND","NOT FOUND","NOT FOUND")
                Toast.makeText(this@NurseListViewFragment, getString(R.string.no_user), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun registerNurse() {
        binding.signupUser.setOnClickListener {
            val intent = Intent(this@NurseListViewFragment, UserRegistration::class.java)
            startActivity(intent)
            finish()
        }
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
                    this@NurseListViewFragment,
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
                for (i in userList.indices) {
                    data = arrayOf(
                        userList[i].faceID.toString(),
                        userList[i].personName.toString(),
                        userList[i].patientRemarks,
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