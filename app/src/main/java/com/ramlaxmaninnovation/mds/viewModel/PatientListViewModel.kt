package com.ramlaxmaninnovation.mds.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ramlaxmaninnovation.mds.R
import com.ramlaxmaninnovation.mds.repository.AppRepository
import com.ramlaxmaninnovation.mds.utils.subUtils.Event
import com.ramlaxmaninnovation.mds.utils.subUtils.Resource
import com.ramlaxmaninnovation.mds.utils.subUtils.Utils
import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientListModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

class PatientListViewModel(
    app: Application,
    private val appRepository: AppRepository
) : AndroidViewModel(app) {

    val productDetailsById: MutableLiveData<Resource<PatientListModel>> = MutableLiveData()

    fun getProductById(id: String) = viewModelScope.launch {
        getProductDetailsById(id)

    }

    private suspend fun getProductDetailsById(id: String) {
        productDetailsById.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                val response = appRepository.fetchDevicePatientList(id)
                Log.i("TAG", "fetchPics: $response")
                productDetailsById.postValue(handlePicsResponse(response))
            } else {
                productDetailsById.postValue(
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
                is IOException -> productDetailsById.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.network_failure
                        )
                    )
                )
                else -> productDetailsById.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.conversion_error
                        )
                    )
                )
            }
        }
    }

    private fun handlePicsResponse(response: Response<PatientListModel>): Resource<PatientListModel> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


}