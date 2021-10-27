package com.ramlaxmaninnovation.mds.network



    import com.ramlaxmaninnovation.mds.getPatientDetails.GetDetailsModel
    import com.ramlaxmaninnovation.mds.registration.RegisterResponse
    import com.ramlaxmaninnovation.mds.utils.ErrorMsg
    import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientListModel
    import com.ramlaxmaninnovation.mds.views.ui.transactionlist.TransactionModel
    import okhttp3.ResponseBody
    import retrofit2.http.*
    import retrofit2.Response
    interface RetrofitInterface {





        @FormUrlEncoded
        @POST("devices")
        suspend fun  addDevice(@Body body: RequestBodies.AddDevice): Response<ErrorMsg>


        @FormUrlEncoded
        @POST("devices")
        suspend fun addDeviceAgain(@Field("device_id")device_id: String) : Response<ErrorMsg>


        @FormUrlEncoded
        @POST("transactions")
        suspend fun addTransaction(@Body body: RequestBodies.AddTransaction): Response<ResponseBody>


        @GET("patientlist/{location}")
        suspend fun fetchPatientList(@Path("location")location:String): Response<PatientListModel>




        @GET("report/{location}")
        suspend fun fetchTransactionList(@Path("location")device_id:String): Response<TransactionModel>

        @FormUrlEncoded
        @POST("patients")
        suspend fun addPatient(@Body body: RequestBodies.AddPatient):Response<RegisterResponse>


        @FormUrlEncoded
        @POST("patients/{id}")
        suspend fun editPatient(@Path("id")device_id:String , @Body body: RequestBodies.EditPatient):Response<RegisterResponse>




    }


