package com.ramlaxmaninnovation.mds.repository

import com.ramlaxmaninnovation.mds.network.RequestBodies
import com.ramlaxmaninnovation.mds.network.ServerConfig

class AppRepository {


    suspend fun fetchDeviceTransactionList(id: String) = ServerConfig.useApi.fetchTransactionList(id)


    suspend fun fetchDevicePatientList(id: String) = ServerConfig.useApi.fetchPatientList(id)


    suspend fun registerPatient(body: RequestBodies.AddPatient) = ServerConfig.useApi.addPatient(body)


}