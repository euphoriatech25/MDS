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
import com.ramlaxmaninnovation.mds.views.ui.transactionlist.TransactionModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class TransactionViewModel(  app: Application,
                             private val appRepository: AppRepository
) : AndroidViewModel(app) {

    val transactionDetailsById: MutableLiveData<Resource<TransactionModel>> = MutableLiveData()


    fun getTransactionById(id: String) = viewModelScope.launch {
        getTransactionDetailsById(id)
    }


    private suspend fun getTransactionDetailsById(id: String) {
        transactionDetailsById.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication<Application>())) {
                val response = appRepository.fetchDeviceTransactionList(id)
                Log.i("TAG", "fetchPics: $response")
                transactionDetailsById.postValue(handlePicsResponse(response))
            } else {
                transactionDetailsById.postValue(
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
                is IOException -> transactionDetailsById.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.network_failure
                        )
                    )
                )
                else -> transactionDetailsById.postValue(
                    Resource.Error(
                        getApplication<Application>().getString(
                            R.string.conversion_error
                        )
                    )
                )
            }
        }
    }

    private fun handlePicsResponse(response: Response<TransactionModel>): Resource<TransactionModel> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}