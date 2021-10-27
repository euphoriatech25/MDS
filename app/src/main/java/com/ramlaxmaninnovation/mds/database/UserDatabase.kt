package com.ramlaxmaninnovation.mds.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database( entities = [User::class], version = 3 )
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDataDAO() : UserDataDAO

}