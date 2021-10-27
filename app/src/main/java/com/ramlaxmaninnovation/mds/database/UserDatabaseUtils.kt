package com.ramlaxmaninnovation.mds.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDatabaseUtils {
    companion object {

        private val DATABASE_NAME = "user_database"


        interface Callback {
            fun onQueryCompleted(users: Array<User>)
        }


        fun insertUser(
            context: Context,
            faceID: String,
            userName: String,
            userEmbedding: String,
            patientRemarks: String,
            photoString: String,
            terminalName: String
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                insertUserAsync(
                    context,
                    faceID,
                    userName,
                    userEmbedding,
                    patientRemarks,
                    photoString,
                    terminalName
                )
            }
        }


        private suspend fun insertUserAsync(
            context: Context,
            faceID: String,
            userName: String,
            userEmbedding: String,
            patientRemarks: String,
            photoString: String,
            terminalName: String
        ) = withContext(Dispatchers.Default) {
            val db = getUserDatabase(context)
            db.userDataDAO().insertUser(
                User(
                    faceID,
                    userName,
                    userEmbedding,
                    patientRemarks,
                    photoString,
                    terminalName
                )
            )
        }


        fun getUsersFromFace(context: Context, callback: Callback) {
            CoroutineScope(Dispatchers.Main).launch {
                val user = getUserAsync(context)
                callback.onQueryCompleted(user.toTypedArray())
            }
        }


        fun getUsersFromFaceID(context: Context, faceID: String, callback: Callback) {
            CoroutineScope(Dispatchers.Main).launch {
                val user = getUserAsyncById(context, faceID)
                callback.onQueryCompleted(user.toTypedArray())
            }
        }


        private suspend fun getUserAsyncById(context: Context, faceID: String): List<User> =
            withContext(Dispatchers.Default) {
                val db = getUserDatabase(context)
                return@withContext db.userDataDAO().getUsersById(faceID)
            }

        private suspend fun getUserAsync(context: Context): List<User> =
            withContext(Dispatchers.Default) {
                val db = getUserDatabase(context)
                return@withContext db.userDataDAO().getUsers()
            }


        private fun getUserDatabase(context: Context): UserDatabase {
            return Room.databaseBuilder(context, UserDatabase::class.java, "$DATABASE_NAME.db")
                .build()
        }




        fun deleteUsersFromFaceID(context: Context, faceID: String){
            CoroutineScope(Dispatchers.Default).launch {
                val db = getUserDatabase(context)
                db.userDataDAO().deleteByUserId(faceID)
            }

        }

    }

}