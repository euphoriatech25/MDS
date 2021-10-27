package com.ramlaxmaninnovation.mds.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ramlaxmaninnovation.mds.utils.Constant
import com.ramlaxmaninnovation.mds.utils.Constant.Companion.BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ServerConfig {
    companion object {

        private val client = buildClient()
        private val retrofit = buildRetrofit(client)

        private fun buildClient(): OkHttpClient {
            val builder:  OkHttpClient.Builder = OkHttpClient.Builder()
                .addInterceptor(object : Interceptor {
                    @Throws(IOException::class)
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request: Request = chain.request()
                        val builder: Request.Builder = request.newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Connection", "close")
                        request = builder.build()
                        return chain.proceed(request)
                    }
                })
            return builder.build()
        }

        private fun buildRetrofit(client: OkHttpClient): Retrofit {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        private fun <T> createService(service: Class<T>?): T {
            return retrofit.create(service)
        }
        val useApi by lazy {
            createService(RetrofitInterface::class.java)
        }
}

    }


