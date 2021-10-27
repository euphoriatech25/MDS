package com.ramlaxmaninnovation.mds.utils;

import static android.provider.Settings.Secure.ANDROID_ID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ramlaxmaninnovation.mds.R;
import com.ramlaxmaninnovation.mds.network.ServiceConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class AppUtils {


    public static void setLocal(Activity activity, String langCode) {
        Log.i("TAG", "setLocal: "+langCode);
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected())
            return true;

        return false;
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static ApiError convertErrors(ResponseBody response) {
        Converter<ResponseBody, ApiError> converter = ServiceConfig.retrofit().responseBodyConverter(ApiError.class, new Annotation[0]);
        ApiError apiError = null;
        try {
            apiError = converter.convert(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiError;
    }
    public static void handleError(ResponseBody errorBody, Context context) {
        ApiError apiErrors = AppUtils.convertErrors(errorBody);
        Log.i("TAG", "handleError: "+apiErrors.getErrors());
        if (errorBody != null) {
            for (Map.Entry<String, List<String>> error : apiErrors.getErrors().entrySet()) {
                if (error.getKey().equals("message")) {
                    Toast.makeText(context, error.getValue().get(0), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
        }
    }




    @SuppressLint("HardwareIds")
    public static String ANDROID_ID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                ANDROID_ID).toUpperCase();
    }


//    public static void popupMessage(@NotNull Context context, boolean status) {
//        Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(true);
//        dialog.setContentView(R.layout.pop_up_face_recognization);
//        ImageView status_display_animation = dialog.findViewById(R.id.status_display_animation);
//
//        if(status){
//            Glide.with(context).asGif().load(R.drawable.success).into(status_display_animation);
//        }else{
//            Glide.with(context).asGif().load(R.drawable.error).into(status_display_animation);
//        }
//        dialog.show();
//    }
}
