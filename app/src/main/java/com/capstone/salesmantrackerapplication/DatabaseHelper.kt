package com.capstone.salesmantrackerapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user_data.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "user_entries"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_MANUAL_LOCATION = "manual_location"
        private const val COLUMN_AUTO_LOCATION = "auto_location"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_PURPOSE = "purpose"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_MANUAL_LOCATION TEXT, "
                + "$COLUMN_AUTO_LOCATION TEXT, "
                + "$COLUMN_TIME TEXT, "
                + "$COLUMN_PURPOSE TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(name: String, manualLocation: String, autoLocation:String, time: String, purpose: String): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_MANUAL_LOCATION, manualLocation)
            put(COLUMN_AUTO_LOCATION, autoLocation)
            put(COLUMN_TIME, time)
            put(COLUMN_PURPOSE, purpose)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getAllData(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }
}
