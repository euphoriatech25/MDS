package com.ramlaxmaninnovation.mds.models

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import android.R.attr.data

class PatientDetails {
    @SerializedName("success")
    @Expose
    private var success: Boolean? = null

    @SerializedName("data")
    @Expose
    private var data: Data? = null

    @SerializedName("message")
    @Expose
    private var message: String? = null

    fun getSuccess(): Boolean? {
        return success
    }

    fun setSuccess(success: Boolean?) {
        this.success = success
    }

    fun getData(): Data? {
        return data
    }

    fun setData(data: Data?) {
        this.data = data
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }
    class Data {
        @SerializedName("device_id")
        @Expose
        var device_id: String? = null


        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("name")
        @Expose
        var name: String? = null


       @SerializedName("remarks")
        @Expose
        var remarks: String? = null


        @SerializedName("photo_string")
        @Expose
        var photo_string: String? = null

        @SerializedName("patient_id")
        @Expose
        var patient_id: String? = null


        @SerializedName("photo")
        @Expose
        var photo: String? = null

        @SerializedName("created_at")
        @Expose
        var createdAt: String? = null

        @SerializedName("updated_at")
        @Expose
        var updatedAt: String? = null

        constructor(
            device_id: String?,
            name: String?,
            patient_id: String?,
            photo: String?,
            remarks: String?,
            photo_string: String?
        ) {
            this.device_id = device_id
            this.name = name
            this.patient_id = patient_id
            this.remarks = remarks
            this.photo = photo
            this.photo_string = photo_string
        }


        override fun toString(): String {
            return "Data(device_id=$device_id, id=$id, name=$name, patient_id=$patient_id, photo=$photo, createdAt=$createdAt, updatedAt=$updatedAt)"
        }


    }
}