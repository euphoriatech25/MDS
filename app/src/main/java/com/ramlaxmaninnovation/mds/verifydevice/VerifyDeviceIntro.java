package com.ramlaxmaninnovation.mds.verifydevice;

import static android.provider.Settings.Secure.ANDROID_ID;
import static com.ramlaxmaninnovation.mds.utils.Constant.USER_DETAILS_PREF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.ramlaxmaninnovation.mds.R;
import com.ramlaxmaninnovation.mds.network.RetroOldApi;
import com.ramlaxmaninnovation.mds.network.ServerConfigSecondary;
import com.ramlaxmaninnovation.mds.network.ServiceConfig;
import com.ramlaxmaninnovation.mds.utils.AppUtils;
import com.ramlaxmaninnovation.mds.utils.ErrorMsg;
import com.ramlaxmaninnovation.mds.utils.UserPrefManager;

import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VerifyDeviceIntro extends AppCompatActivity {

    Button verify_device;
    SwitchCompat languageSwitch;

    Dialog dialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor shareEditor;
    ProgressDialog pd;
    UserPrefManager userPrefManager;
   String selectedLocation;
    @SuppressLint("HardwareIds")
    public static String ANDROID_ID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                ANDROID_ID).toUpperCase();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_verification);
        verify_device = findViewById(R.id.register);
        dialog = new Dialog(this);
        languageSwitch = findViewById(R.id.languageSwitch);

        pd = new ProgressDialog(VerifyDeviceIntro.this);
        userPrefManager = new UserPrefManager(this);

        languageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    languageSwitch.setText(languageSwitch.getTextOn());
                    userPrefManager.setLanguage("en");
                    AppUtils.setLocal(VerifyDeviceIntro.this, "en");
                } else {
                    languageSwitch.setText(languageSwitch.getTextOff());
                    userPrefManager.setLanguage("ja");
                    AppUtils.setLocal(VerifyDeviceIntro.this, "ja");
                }
            }
        });




        if (restorePrefData()) {
            Intent mainActivity = new Intent(getApplicationContext(), CameraViewActivity.class);
            startActivity(mainActivity);
            finish();

        } else {
//            popupMessage();
            verify_device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AppUtils.hideKeyboard(VerifyDeviceIntro.this);
                    if (selectedLocation.length() > 0) {
                        String location = selectedLocation;
                        reRegisterDevice(location, ANDROID_ID(VerifyDeviceIntro.this));
                    } else {
                        Toast.makeText(VerifyDeviceIntro.this, getString(R.string.add_your_device_location), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        getLocationList();
    }

    private void getLocationList() {

        if (AppUtils.isNetworkAvailable(this)) {
            RetroOldApi post = ServiceConfig.createService(RetroOldApi.class);
            Call<GetLocationList> call = post.fetchLocationList();
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<GetLocationList> call, Response<GetLocationList> response) {
                    if (response.isSuccessful()) {
                        GetLocationList getLocationList = response.body();
                       List<String>location=new ArrayList<>();
                        if (getLocationList.getData().size() != 0) {
                            location.addAll(getLocationList.getData());
                            getLanguageChange(location);
                        }
                    }
                }
                @Override
                public void onFailure(Call<GetLocationList> call, Throwable t) {
                    Toast.makeText(VerifyDeviceIntro.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(VerifyDeviceIntro.this, getString(R.string.no_interest_connection), Toast.LENGTH_SHORT).show();
        }
    }


    private void getLanguageChange(List<String> getLocation) {
        Spinner spinCountry;
        spinCountry = (Spinner) findViewById(R.id.spinLocation);//fetch the spinner from layout file
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.languages,getLocation);
        adapter.setDropDownViewResource(R.layout.language);
        spinCountry.setAdapter(adapter);
        spinCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                selectedLocation=adapter.getItem(position).toString();


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                selectedLocation=adapter.getItem(0).toString();
            }
        });
    }

    private void registerDevice(String location, String android_id) {

        if (AppUtils.isNetworkAvailable(this)) {
            RetroOldApi post = ServiceConfig.createService(RetroOldApi.class);
            Call<ResponseBody> call = post.addDevice(android_id, location);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    pd.dismiss();

                    if (response.isSuccessful()) {
                        userPrefManager.setLocation(location);
                        savePrefsData();
                        startActivity(new Intent(VerifyDeviceIntro.this, CameraViewActivity.class));
                        finish();
                    } else if (response.code() == 404) {
                        AppUtils.convertErrors(response.errorBody());
                    } else {
                        Toast.makeText(VerifyDeviceIntro.this, response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("TAG", "onFailure: " + t.getLocalizedMessage());
                    Toast.makeText(VerifyDeviceIntro.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(VerifyDeviceIntro.this, getString(R.string.no_interest_connection), Toast.LENGTH_SHORT).show();
        }
    }


    private void reRegisterDevice(String location, String android_id) {
        pd.setTitle(getString(R.string.registering_device));
        pd.setMessage(getString(R.string.plase_wait));
        pd.show();
        userPrefManager.setTerminalName(location);
        if (AppUtils.isNetworkAvailable(this)) {
            RetroOldApi post = ServerConfigSecondary.createService(RetroOldApi.class);
            Call<ErrorMsg> call = post.addDeviceAgain(android_id);
            call.enqueue(new Callback<ErrorMsg>() {
                @Override
                public void onResponse(Call<ErrorMsg> call, Response<ErrorMsg> response) {

                    if (response.isSuccessful()) {
                        registerDevice(location, ANDROID_ID(VerifyDeviceIntro.this));
                    } else if (response.code() == 404) {
                        try {
                            JSONObject obj = new JSONObject(response.body().toString());
                            JSONObject subError = new JSONObject(obj.getJSONObject("errors").getString("message"));
                            UserPrefManager userPrefManager = new UserPrefManager(VerifyDeviceIntro.this);

                            if (userPrefManager.getLanguage().equalsIgnoreCase("en")) {
                                Toast.makeText(VerifyDeviceIntro.this, subError.getString("en"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VerifyDeviceIntro.this, subError.getString("jpn"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(VerifyDeviceIntro.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(VerifyDeviceIntro.this, response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ErrorMsg> call, Throwable t) {
                    pd.dismiss();
                    if (t instanceof SocketTimeoutException) {
                        Toast.makeText(VerifyDeviceIntro.this, "socket timeout", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VerifyDeviceIntro.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            pd.dismiss();
//            AppUtils.getNetworkCheck(this);
        }
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(USER_DETAILS_PREF, MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isIntroOpnend", false);
        return isIntroActivityOpnendBefore;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(USER_DETAILS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpnend", true);
        editor.apply();
    }


}
