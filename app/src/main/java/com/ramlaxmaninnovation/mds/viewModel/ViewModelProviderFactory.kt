package com.ramlaxmaninnovation.mds.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ramlaxmaninnovation.mds.repository.AppRepository
import com.ramlaxmaninnovation.mds.views.ui.patientlist.PatientAdapter

class ViewModelProviderFactory(val app: Application,
                               val appRepository: AppRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NurseListViewModel::class.java)) {
            return NurseListViewModel(app, appRepository) as T
        }

        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(app, appRepository) as T
        }

        if (modelClass.isAssignableFrom(PatientListViewModel::class.java)) {
            return PatientListViewModel(app, appRepository) as T
        }


   if (modelClass.isAssignableFrom(RegisterPatientViewModel::class.java)) {
            return RegisterPatientViewModel(app, appRepository) as T
        }

        throw IllegalArgumentException("Unknown class name")
    }

}