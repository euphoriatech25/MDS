package com.ramlaxmaninnovation.mds.views.ui.patientlist


import com.google.gson.annotations.SerializedName

data class PatientListModel (

	@SerializedName("success") val success : Boolean,
	@SerializedName("data") val data : List<Data>,
	@SerializedName("message") val message : String
)

data class Data (
	@SerializedName("patient_id ") val patient_id  : String,
	@SerializedName("name") val name : String,
	@SerializedName("remarks") val remarks : String,
	@SerializedName("device_name") val device_name : String,
	@SerializedName("photo") val photo : String,
	@SerializedName("photo_string") val photo_string : String
)