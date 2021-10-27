package com.ramlaxmaninnovation.mds.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDataDAO {

    @Insert
    fun insertUser( user : User )

    @Query( "SELECT * from user_data_table " )
    fun getUsers() : List<User>


    @Query( "SELECT * from user_data_table WHERE faceID = :userFaceID" )
    fun getUsersById( userFaceID : String ) : List<User>

    @Query("DELETE FROM user_data_table WHERE faceID = :userFaceID")
    fun deleteByUserId( userFaceID:String)

//    @Query("UPDATE FROM user_data_table WHERE user_id = :userId")
//    fun updateByUserId(long userId)
}