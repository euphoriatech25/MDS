package com.ramlaxmaninnovation.mds.utils



internal class Constant {
    companion object {
         const val LANGUAGE: String="language"
         const val NETWORK: String="network"
        const val BASE_URL_LIMIT = "http://202.52.240.148:5062/mds_limit/public/api/" /// production
        const val BASE_URL = "http://202.52.240.148:5062/medical_dispatch_system/public/api/" /// production
        const val USER_DETAILS_PREF = "user_details_pref"
        const val CONNECTION_TIME_OUT: Long = 500
        const val THRESHOLD: String = "threshold"
    }
}