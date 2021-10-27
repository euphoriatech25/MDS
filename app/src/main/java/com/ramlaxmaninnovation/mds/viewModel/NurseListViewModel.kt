package com.ramlaxmaninnovation.mds.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ramlaxmaninnovation.mds.repository.AppRepository

class NurseListViewModel (
    app: Application,
    private val appRepository: AppRepository
    ) : AndroidViewModel(app) {

//        val picsData: MutableLiveData<Resource<NurseListModel>> = MutableLiveData()
//
//        init {
//            getSliderPictures()
//        }
//
//        private fun getSliderPictures() = viewModelScope.launch {
//            fetchPics()
//        }
//
//
//        private suspend fun fetchPics() {
//            picsData.postValue(Resource.Loading())
//            try {
//                if (Utils.hasInternetConnection(Application)) {
//                    val response = appRepository.get()
//                    picsData.postValue(handlePicsResponse(response))
//                } else {
//                    picsData.postValue(Resource.Error(getApplication<MyApplication>().getString(R.string.no_interest_connection)))
//                }
//            } catch (t: Throwable) {
//                when (t) {
//                    is IOException -> picsData.postValue(
//                        Resource.Error(
//                            getApplication<MyApplication>().getString(
//                                R.string.network_failure
//                            )
//                        )
//                    )
//                    else -> picsData.postValue(
//                        Resource.Error(
//                            getApplication<MyApplication>().getString(
//                                R.string.conversion_error
//                            )
//                        )
//                    )
//                }
//            }
//        }
//
//        private fun handlePicsResponse(response: Response<NurseListModel>): Resource<NurseListModel> {
//            if (response.isSuccessful) {
//                response.body()?.let { resultResponse ->
//                    return Resource.Success(resultResponse)
//                }
//            }
//            return Resource.Error(response.message())
//        }


    }