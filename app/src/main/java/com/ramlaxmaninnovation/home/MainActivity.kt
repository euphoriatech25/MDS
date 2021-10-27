package com.ramlaxmaninnovation.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.ActivityMainBinding
import com.ramlaxmaninnovation.mds.setting.SettingViewFragment
import com.ramlaxmaninnovation.mds.verifydevice.CameraViewActivity
import com.ramlaxmaninnovation.mds.views.ui.nurselist.NurseListViewFragment
import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientDetailsFragment
import com.ramlaxmaninnovation.mds.views.ui.transactionlist.TransactionListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        changeStatusBarColor()

    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }

    fun onClickPatientList(view: View) {
        val intent = Intent(this, PatientDetailsFragment::class.java).apply {
            intent.putExtra("from","Register")
        }
        startActivity(intent)
    }

    fun onClickUserList(view: View) {
        val intent = Intent(this, NurseListViewFragment::class.java).apply {
        }
        startActivity(intent)
    }

    fun onClickTransactionFace(view: View) {
        val intent = Intent(this, TransactionListFragment::class.java).apply {
        }
        startActivity(intent)
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