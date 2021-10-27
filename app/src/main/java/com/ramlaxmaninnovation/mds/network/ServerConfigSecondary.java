package com.ramlaxmaninnovation.mds.network;

import static com.ramlaxmaninnovation.mds.utils.Constant.BASE_URL_LIMIT;
import static com.ramlaxmaninnovation.mds.utils.Constant.CONNECTION_TIME_OUT;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerConfigSecondary {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.MINUTES)
            .readTimeout(CONNECTION_TIME_OUT, TimeUnit.MINUTES)
            .writeTimeout(CONNECTION_TIME_OUT, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true);

    private static HttpLoggingInterceptor logging =
            new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL_LIMIT)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.client(httpClient.build()).build();

    public static <S> S createService(Class<S> serviceClass) {
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            builder.addConverterFactory(GsonConverterFactory.create(gson));
            builder.client(httpClient.build());
            retrofit = builder.build();
        }
        return retrofit.create(serviceClass);
    }
    public static Retrofit retrofit() {
        return retrofit;
    }
}

