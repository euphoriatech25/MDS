package com.ramlaxmaninnovation.mds.registration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private Message message;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
    public class Message {

        @SerializedName("en")
        @Expose
        private String en;
        @SerializedName("jpn")
        @Expose
        private String jpn;

        public String getEn() {
            return en;
        }

        public void setEn(String en) {
            this.en = en;
        }

        public String getJpn() {
            return jpn;
        }

        public void setJpn(String jpn) {
            this.jpn = jpn;
        }

    }
    }