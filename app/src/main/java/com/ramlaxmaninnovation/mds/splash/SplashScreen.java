package com.ramlaxmaninnovation.mds.splash;


//import static com.ramlaxmaninnovation.mds.utils.Constant.USER_DETAILS_PREF;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.PackageInfoCompat;

import com.ramlaxmaninnovation.mds.R;
import com.ramlaxmaninnovation.mds.utils.AppUtils;
import com.ramlaxmaninnovation.mds.utils.UserPrefManager;
import com.ramlaxmaninnovation.mds.verifydevice.VerifyDeviceIntro;

public class SplashScreen extends AppCompatActivity {
    private static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;

    private static int SPLASH_TIME_OUT = 1300;
    private ImageView imageView;
    TextView  slogin,versionName;
    Animation top, button;
   UserPrefManager userPrefManager;

   private SharedPreferences prefs;
    public static String getAppVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = (int) PackageInfoCompat.getLongVersionCode(info);
            return context.getString(R.string.version)+" :- " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
         slogin = findViewById(R.id.slogan);

        versionName = findViewById(R.id.versionName);
        versionName.setText(getAppVersionName(this));
         imageView = findViewById(R.id.companyLogo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userPrefManager = new UserPrefManager(this);
        String value= userPrefManager.getLanguage();
        AppUtils.setLocal(this , value);
        Log.i("TAG", "onCreate: "+value);
        goToHome();

    }


    public void goToHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), VerifyDeviceIntro.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
            }
        }, SPLASH_TIME_OUT);
    }
}
