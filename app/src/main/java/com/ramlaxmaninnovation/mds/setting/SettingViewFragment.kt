package com.ramlaxmaninnovation.mds.setting

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ramlaxmaninnovation.home.MainActivity
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.databinding.FragmentSettingViewBinding
import com.ramlaxmaninnovation.mds.splash.SplashScreen
import com.ramlaxmaninnovation.mds.utils.Constant
import com.ramlaxmaninnovation.mds.utils.UserPrefManager

class SettingViewFragment : AppCompatActivity() {
    var userPrefManager: UserPrefManager? = null
    private lateinit var binding: FragmentSettingViewBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
       changeStatusBarColor()
        userPrefManager = UserPrefManager(this@SettingViewFragment)

        binding.onBckPress.setOnClickListener {  val intent = Intent(this, MainActivity::class.java).apply {

        }
            startActivity(intent) }
        checkOnlineOfflineStatus()
        getLanguageChange()
        getThrehold()
    }
    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
    }
    private fun getThrehold() {


        var threshold = userPrefManager?.threshold?.toDouble()
        if (threshold != null) {
            binding.textviewThreshold.text = (threshold).toString()

            binding.buttonMinusThreshold.setOnClickListener {

                if (threshold > 0.01) {
                    threshold -= 0.01
                    binding.textviewThreshold.text = (threshold).toString()
                    userPrefManager!!.setThreshold((threshold).toString())
                }
            }


            binding.buttonPlusThreshold.setOnClickListener {
                threshold += 0.01
                binding.textviewThreshold.text = (threshold).toString()
                userPrefManager!!.setThreshold((threshold).toString())
            }


            binding.aboutDeveloper.setOnClickListener {
                showCustomDialog(this@SettingViewFragment)
            }
        }
    }

    fun showCustomDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.about_us)

        dialog.show()
    }

    private fun checkOnlineOfflineStatus() {


        if (userPrefManager!!.networkStatus) {
            binding.status.setText(getString(R.string.online))
        } else {
            binding.status.setText(getString(R.string.offline))
        }

        binding.onlineofflineswitch.setOnCheckedChangeListener { _, isChecked ->

            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {


                            if (isChecked) {

                                userPrefManager?.clearData()
                                val intent =
                                    Intent(this@SettingViewFragment, SplashScreen::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK //this will always start your activity as a new task
                                intent.putExtra(Constant.LANGUAGE, getString(R.string.online))
                                startActivity(intent)

                            } else {
                                userPrefManager?.clearData()
                                val intent =
                                    Intent(this@SettingViewFragment, SplashScreen::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK //this will always start your activity as a new task
                                intent.putExtra(Constant.LANGUAGE, getString(R.string.offline))
                                startActivity(intent)

                            }

                        }

                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this@SettingViewFragment)
            builder.setMessage(getString(R.string.onlineSwitch))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show()

        }
    }


    private fun getLanguageChange() {

        val languages = arrayOf(getString(R.string.language_change), "English", "日本")
//        val adapter1 = ArrayAdapter(this, R.layout.languages, languages)
        val adapter1 = this?.let {
            ArrayAdapter<String>(
                it,
                R.layout.languages,
                languages
            )
        }

        adapter1?.setDropDownViewResource(R.layout.language)
        binding.languageChange.adapter = adapter1
        binding.languageChange.setSelection(0)
        binding.languageChange.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    val selectedLang = adapterView.getItemAtPosition(i).toString()
                    if (selectedLang == "English") {
                        userPrefManager!!.language = "en"

                        val intent = Intent(this@SettingViewFragment, SplashScreen::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                    } else if (selectedLang == "日本") {

                        userPrefManager!!.language = "ja"

                        val intent = Intent(this@SettingViewFragment, SplashScreen::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
    }

}