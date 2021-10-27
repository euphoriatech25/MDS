package com.ramlaxmaninnovation.mds.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ErrorMsg {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("errors")
    @Expose
    private Errors errors;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }
    public class Errors {

        @SerializedName("message")
        @Expose
        private List<String> message = null;

        public List<String> getMessage() {
            return message;
        }

        public void setMessage(List<String> message) {
            this.message = message;
        }

    }


    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("device_id")
    @Expose
    private String device_id;

    public ErrorMsg(String location, String device_id) {
        this.location = location;
        this.device_id = device_id;
    }
}
