package com.ramlaxmaninnovation.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.ActivityMainBinding
import com.ramlaxmaninnovation.mds.setting.SettingViewFragment
import com.ramlaxmaninnovation.mds.utils.UserPrefManager
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import com.ramlaxmaninnovation.mds.views.ui.nurselist.NurseListViewFragment
import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientDetailsFragment
import com.ramlaxmaninnovation.mds.views.ui.transactionlist.TransactionListFragment
import kotlinx.android.synthetic.main.content_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var userPrefManager: UserPrefManager? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPrefManager= UserPrefManager(this)

        binding.appBarMain.clMain.ic_setting.setOnClickListener {
            startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)

        }
        if(userPrefManager!!.cameraView.equals("back")){
           binding.appBarMain.clMain.cameraSwitch.isChecked=true
           binding.appBarMain.clMain.cameraSwitch.text =binding.appBarMain.clMain.cameraSwitch.textOn

        }else{
           binding.appBarMain.clMain.cameraSwitch.isChecked=false
           binding.appBarMain.clMain.cameraSwitch.text =binding.appBarMain.clMain.cameraSwitch.textOff
        }

       binding.appBarMain.clMain.cameraSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
               binding.appBarMain.clMain.cameraSwitch.text =binding.appBarMain.clMain.cameraSwitch.textOn
               userPrefManager!!.cameraView = "back"

            } else {
               binding.appBarMain.clMain.cameraSwitch.text =binding.appBarMain.clMain.cameraSwitch.textOff
               userPrefManager!!.cameraView = "front"

            }
        })

//        setSupportActionBar(binding.appBarMain.toolbar)
        changeStatusBarColor()

    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    fun onClickPatientList(view: View) {
        if (userPrefManager?.nurseDetails?.get(1).equals("NOT FOUND")) {

            Toast.makeText(this,getString(R.string.no_user_login), Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, PatientDetailsFragment::class.java).apply {
                intent.putExtra("from","Register")
            }
            startActivity(intent)

        }

    }

    fun onClickUserList(view: View) {
        val intent = Intent(this, NurseListViewFragment::class.java).apply {
        }
        startActivity(intent)
    }

    fun onClickTransactionFace(view: View) {
        if (userPrefManager?.nurseDetails?.get(1).equals("NOT FOUND")) {

            Toast.makeText(this,getString(R.string.no_user_login), Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, TransactionListFragment::class.java).apply {
            }
            startActivity(intent)
        }


    }

    fun onClickScanSetting(view: View) {
        val intent = Intent(this, SettingViewFragment::class.java).apply {
        }
        startActivity(intent)
    }
  fun onClickBackToScanner(view: View) {
        val intent = Intent(this, CameraViewActivity::class.java).apply {
        }
        startActivity(intent)
      finish()
    }
    fun onBackPress(view: View) {
        val intent = Intent(this@MainActivity, CameraViewActivity::class.java)
        startActivity(intent)
        finish()
    }


}