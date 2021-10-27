package com.ramlaxmaninnovation.mds.ml

import android.util.Base64
import java.nio.charset.StandardCharsets

class EmbeddingUtils {

    companion object {


        fun floatArrayToString(array: FloatArray): String {
            val out = StringBuilder()
            for (x in array) {
                out.append("$x,")
            }
            return out.toString()
        }


        fun stringToFloatArray(string: String, length: Int): FloatArray {
            val floats = string.split(",").toTypedArray()
            val out = FloatArray(length)
            for (i in 0 until length) {
                out[i] = floats[i].trim { it <= ' ' }.toFloat()
            }
            return out
        }

    }

}