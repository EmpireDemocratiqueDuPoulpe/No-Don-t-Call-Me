package com.eddp.nodontcallme.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.eddp.nodontcallme.interfaces.DbObserver
import com.eddp.nodontcallme.interfaces.ObservableDb
import java.lang.Exception

class DatabaseHandler private constructor(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), ObservableDb {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_MISSED_CALLS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_MISSED_CALLS")
            this.onCreate(db)
        }
    }

    // Functions for operations on database
    fun addMissedCall(missedCall: MissedCall.CallItem) {
        val db: SQLiteDatabase = writableDatabase

        // Update if the record already exist
        if (getMissedCall(missedCall.phoneNumber) != null) {
            return updateMissedCall(missedCall)
        }

        db.beginTransaction()
        try {
            val data = ContentValues()
            data.put(KEY_MISSED_CALLS_PHONE_NUMBER, missedCall.phoneNumber)
            data.put(KEY_MISSED_CALLS_COUNT, missedCall.callsCount)

            db.insertOrThrow(TABLE_MISSED_CALLS, null, data)
            db.setTransactionSuccessful()
        } catch (err: Exception) {
            Log.e("DB", "Exception: ${err.message}", err)
        } finally {
            db.endTransaction()
            notifyChange()
        }
    }

    fun getMissedCalls() : MutableList<MissedCall.CallItem> {
        val db: SQLiteDatabase = readableDatabase
        val missedCalls: MutableList<MissedCall.CallItem> = ArrayList()

        val MISSED_CALLS_SELECT_QUERY =
            "SELECT $KEY_MISSED_CALLS_ID, $KEY_MISSED_CALLS_PHONE_NUMBER, $KEY_MISSED_CALLS_COUNT " +
            "FROM $TABLE_MISSED_CALLS"

        val cursor = db.rawQuery(MISSED_CALLS_SELECT_QUERY, null)

        // Exec the query
        try {
            if (cursor.moveToFirst()) {
                do {
                    missedCalls.add(
                        MissedCall.CallItem(
                            cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_ID)), // Id
                            cursor.getString(cursor.getColumnIndex(KEY_MISSED_CALLS_PHONE_NUMBER)), // Phone number
                            cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_COUNT)) // Calls count
                        )
                    )
                } while (cursor.moveToNext())
            }
        } catch (err: Exception) {
            Log.e("DB", "Exception: ${err.message}", err)
        } finally {
            cursor.close()
        }

        return missedCalls
    }

    fun getMissedCall(phoneNumber: String) : MissedCall.CallItem? {
        val db = readableDatabase
        var missedCall: MissedCall.CallItem? = null

        val MISSED_CALL_SELECT_QUERY =
            "SELECT $KEY_MISSED_CALLS_ID, $KEY_MISSED_CALLS_PHONE_NUMBER, $KEY_MISSED_CALLS_COUNT " +
            "FROM $TABLE_MISSED_CALLS " +
            "WHERE $KEY_MISSED_CALLS_PHONE_NUMBER = $phoneNumber"

        val cursor = db.rawQuery(MISSED_CALL_SELECT_QUERY, null)

        // Exec the query
        try {
            if (cursor.moveToFirst()) {
                missedCall = MissedCall.CallItem(
                    cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_ID)), // Id
                    cursor.getString(cursor.getColumnIndex(KEY_MISSED_CALLS_PHONE_NUMBER)), // Phone number
                    cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_COUNT)) // Calls count
                )
            }
        } catch (err: Exception) {
            Log.e("DB", "Exception: ${err.message}", err)
        } finally {
            cursor.close()
        }

        return missedCall
    }

    fun updateMissedCall(missedCall: MissedCall.CallItem) {
        val currentRow: MissedCall.CallItem? = this.getMissedCall(missedCall.phoneNumber)

        if (currentRow != null) {
            val db = writableDatabase

            val data = ContentValues()
            data.put(KEY_MISSED_CALLS_COUNT, currentRow.callsCount + 1)

            val WHERE_ARGS: Array<String> = arrayOf(currentRow.missedCallId.toString())
            val WHERE_CLAUSE = "$KEY_MISSED_CALLS_ID = ?"

            db.update(TABLE_MISSED_CALLS, data, WHERE_CLAUSE, WHERE_ARGS)
            notifyChange()
        }
    }

    fun deleteAll() {
        val db = writableDatabase
        db.beginTransaction()

        try {
            db.delete(TABLE_MISSED_CALLS, null, null)
            db.setTransactionSuccessful()
        } catch (err: Exception) {
            Log.e("DB", "Exception: ${err.message}", err)
        } finally {
            db.endTransaction()
            notifyChange()
        }
    }

    // Observers
    private val dbObserverList: MutableList<DbObserver?> = ArrayList()

    override fun notifyChange() {
        for (dbObserver in this.dbObserverList) {
            dbObserver?.onDatabaseChanged()
        }
    }

    override fun registerObserver(dbObserver: DbObserver) {
        if (!this.dbObserverList.contains(dbObserver)) {
            this.dbObserverList.add(dbObserver)
        }
    }

    override fun removeObserver(dbObserver: DbObserver) {
        if (this.dbObserverList.contains(dbObserver)) {
            this.dbObserverList.remove(dbObserver)
        }
    }

    companion object {
        @Synchronized
        fun getInstance(context: Context): DatabaseHandler? {
            if (instance == null) {
                instance = DatabaseHandler(context.applicationContext)
            }

            return instance
        }

        // Database instance
        private var instance: DatabaseHandler? = null

        // Database info
        const val DATABASE_NAME: String = "noDontCallMeDB"
        const val DATABASE_VERSION: Int = 2

        // Tables names
        const val TABLE_MISSED_CALLS: String = "missed_calls"

        // missed_calls table columns
        const val KEY_MISSED_CALLS_ID: String = "missed_calls_id"
        const val KEY_MISSED_CALLS_PHONE_NUMBER: String = "phone_number"
        const val KEY_MISSED_CALLS_COUNT: String = "count"

        // Create table queries
        const val CREATE_MISSED_CALLS_TABLE: String =
            "CREATE TABLE $TABLE_MISSED_CALLS" +
                    "(" +
                        "$KEY_MISSED_CALLS_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "$KEY_MISSED_CALLS_PHONE_NUMBER TEXT," +
                        "$KEY_MISSED_CALLS_COUNT INTEGER" +
                    ")"
    }
}