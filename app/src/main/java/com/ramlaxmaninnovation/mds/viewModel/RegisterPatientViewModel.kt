package com.ramlaxmaninnovation.mds.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.network.RequestBodies
import com.ramlaxmaninnovation.mds.registration.RegisterResponse
import com.ramlaxmaninnovation.mds.repository.AppRepository
import com.ramlaxmaninnovation.mds.utils.subUtils.Resource
import com.ramlaxmaninnovation.mds.utils.subUtils.Utils
import com.ramlaxmaninnovation.mds.views.ui.transactionlist.TransactionModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

class RegisterPatientViewModel(app: Application,
                               private val appRepository: AppRepository
) : AndroidViewModel(app) {

    val addPatientDetails: MutableLiveData<Resource<RegisterResponse>> = MutableLiveData()


    fun addPatient(body: RequestBodies.AddPatient) = viewModelScope.launch {
        addPatientDetails(body)
    }


    public suspend fun addPatientDetails(body: RequestBodies.AddPatient) {
        addPatientDetails.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                val response = appRepository.registerPatient(body)
                Log.i("TAG", "fetchPics: $response")
                addPatientDetails.postValue(handlePicsResponse(response))
            } else {
                addPatientDetails.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.no_interest_connection
                        )
                    )
                )
            }
        } catch (t: Throwable) {
            Log.i("TAG", "fetchPics: " + t.localizedMessage)
            when (t) {
                is IOException -> addPatientDetails.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.network_failure
                        )
                    )
                )
                else -> addPatientDetails.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.conversion_error
                        )
                    )
                )
            }
        }
    }

    private fun handlePicsResponse(response: Response<RegisterResponse>): Resource<RegisterResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}