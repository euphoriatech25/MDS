package com.ramlaxmaninnovation.mds.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data_table")
data class User (

    @ColumnInfo( name = "faceID" )
    val faceID : String,

    @ColumnInfo( name = "personName" )
    val personName: String? ,

    @ColumnInfo( name = "embedding" )
    val embedding: String? ,

    @ColumnInfo( name = "patientRemarks" )
    val patientRemarks : String,

    @ColumnInfo( name = "photoString" )
    val photostring: String? ,

    @ColumnInfo( name = "terminalName" )
    val terminalName : String,

    @PrimaryKey( autoGenerate = true )
    val id: Int = 0


)