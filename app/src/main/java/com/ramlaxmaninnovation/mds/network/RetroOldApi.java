package com.ramlaxmaninnovation.mds.network;

import com.ramlaxmaninnovation.mds.getPatientDetails.GetDetailsModel;
import com.ramlaxmaninnovation.mds.registration.RegisterResponse;
import com.ramlaxmaninnovation.mds.utils.ErrorMsg;
import com.ramlaxmaninnovation.mds.verifydevice.GetLocationList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetroOldApi {

    @FormUrlEncoded
    @POST("devices")
    Call<ErrorMsg> addDeviceAgain(@Field("device_id") String device_id);


    @FormUrlEncoded
    @POST("devices")
    Call<ResponseBody> addDevice(@Field("device") String device_id, @Field("location") String location, @Field("device_name") String device_name);

    @FormUrlEncoded
    @POST("availability")
    Call<GetDetailsModel> checkAvailability(@Field("location") String location,@Field("patient_id") String patient_id);


    @GET("patient/show/{patient}/{location}")
    Call<GetDetailsModel> fetchData(@Path("patient") String patient_id,@Path("location") String location);


    @FormUrlEncoded
    @POST("transactions")
    Call<ResponseBody> addTransaction(@Field("patient_id") String patient_id,
                                      @Field("device_id") String device_id,
                                      @Field("date_time") String date_time,
                                      @Field("nurse") String nurse,
                                      @Field("location") String location);


    @FormUrlEncoded
    @POST("patients")
    Call<RegisterResponse> addPatient(@Field("device_id") String device_id,
                                      @Field("name") String name,
                                      @Field("photo") String photo,
                                      @Field("patient_id") String patient_id,
                                      @Field("remarks") String remarks,
                                      @Field("photo_string") String photo_string,
                                      @Field("location") String location);


    @FormUrlEncoded
    @POST("update/patient")
    Call<RegisterResponse> editPatient(@Field("device_id") String device_id,
                                       @Field("patient_id") String patient_id,
                                       @Field("name") String name,
                                       @Field("photo") String photo,
                                       @Field("remarks") String remarks,
                                       @Field("photo_string") String photo_string,
                                       @Field("location") String location);

    @FormUrlEncoded
    @POST("delete/patient")
    Call<ErrorMsg> deletePatient(@Field("location") String device_id,
                                 @Field("patient_id") String name);


    @GET("/medical_dispatch_system/public/api/get/location")
    Call<GetLocationList> fetchLocationList();


}
