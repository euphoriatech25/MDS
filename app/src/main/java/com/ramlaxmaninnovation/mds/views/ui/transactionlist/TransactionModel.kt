package com.ramlaxmaninnovation.mds.views.ui.transactionlist

import com.google.gson.annotations.SerializedName


data class TransactionModel (

	@SerializedName("success") val success : Boolean,
	@SerializedName("data") val data : List<Data>,
	@SerializedName("message") val message : String
)
data class Data (
	@SerializedName("device_name") val device_name : String,
	@SerializedName("name") val name : String,
	@SerializedName("patient_name") val patient_name : String,
	@SerializedName("patient_id") val patient_id : String,
	@SerializedName("nurse") val nurse : String,
	@SerializedName("last_consumption_date") val last_consumption_date : String
)