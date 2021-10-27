package com.ramlaxmaninnovation.mds.network

class RequestBodies {

    data class AddTransaction(
        val patient_id: String,
        val device_id: String,
        val date_time: String)

    data class AddDevice(
        val device: String,
        val location: String)

    data class AddPatient(
        val device_id: String,
        val name: String,
        val photo: String,
        val patient_id: String,
        val remarks: String,
        val photo_string: String
    )
data class EditPatient(
        val device_id: String,
        val name: String,
        val photo: String,
        val patient_id: String,
        val remarks: String,
        val photo_string: String
    )


}