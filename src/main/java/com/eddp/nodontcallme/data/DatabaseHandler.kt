package com.eddp.nodontcallme.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Exception

//class DatabaseHandler(
//    context: Context?,
//    factory: SQLiteDatabase.CursorFactory?,
//    errorHandler: DatabaseErrorHandler?
//) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION, errorHandler) {
class DatabaseHandler private constructor(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
    fun addMissedCall(missedCall: MissedCall) {
        val db: SQLiteDatabase = writableDatabase
        db.beginTransaction()

        // Update if the record already exist
        if (getMissedCall(missedCall.phoneNumber) != null) {
            return updateMissedCall(missedCall)
        }

        try {
            val data = ContentValues()
            data.put(KEY_MISSED_CALLS_PHONE_NUMBER, missedCall.phoneNumber)
            data.put(KEY_MISSED_CALLS_COUNT, missedCall.callsCount)

            db.insertOrThrow(TABLE_MISSED_CALLS, null, data)
            db.setTransactionSuccessful()
        } catch (err: Exception) {
            // CATCH
        } finally {
            db.endTransaction()
        }
    }

    fun getMissedCalls() : MutableList<MissedCall> {
        val db: SQLiteDatabase = readableDatabase
        val missedCalls: MutableList<MissedCall> = ArrayList()

        val MISSED_CALLS_SELECT_QUERY: String =
            "SELECT $KEY_MISSED_CALLS_ID, $KEY_MISSED_CALLS_PHONE_NUMBER, $KEY_MISSED_CALLS_COUNT " +
            "FROM $TABLE_MISSED_CALLS"

        val cursor: Cursor = db.rawQuery(MISSED_CALLS_SELECT_QUERY, null)

        // Exec the query
        try {
            if (cursor.moveToFirst()) {
                do {
                    missedCalls.add(
                        MissedCall(
                            cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_ID)), // Id
                            cursor.getString(cursor.getColumnIndex(KEY_MISSED_CALLS_PHONE_NUMBER)), // Phone number
                            cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_COUNT)) // Calls count
                        )
                    )
                } while (cursor.moveToNext())
            }
        } catch (err: Exception) {
            // CATCH
        } finally {
            cursor.close()
        }

        return missedCalls
    }

    fun getMissedCall(phoneNumber: String) : MissedCall? {
        val db: SQLiteDatabase = readableDatabase
        var missedCall: MissedCall? = null

        val MISSED_CALL_SELECT_QUERY: String =
            "SELECT $KEY_MISSED_CALLS_ID, $KEY_MISSED_CALLS_PHONE_NUMBER, $KEY_MISSED_CALLS_COUNT " +
            "FROM $TABLE_MISSED_CALLS " +
            "WHERE $KEY_MISSED_CALLS_PHONE_NUMBER = $phoneNumber"

        val cursor: Cursor = db.rawQuery(MISSED_CALL_SELECT_QUERY, null)

        // Exec the query
        try {
            if (cursor.moveToFirst()) {
                missedCall = MissedCall(
                    cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_ID)), // Id
                    cursor.getString(cursor.getColumnIndex(KEY_MISSED_CALLS_PHONE_NUMBER)), // Phone number
                    cursor.getInt(cursor.getColumnIndex(KEY_MISSED_CALLS_COUNT)) // Calls count
                )
            }
        } catch (err: Exception) {
            // CATCH
        } finally {
            cursor.close()
        }

        return missedCall
    }

    fun updateMissedCall(missedCall: MissedCall) {
        val db: SQLiteDatabase = writableDatabase

        val data = ContentValues()
        data.put(KEY_MISSED_CALLS_COUNT, missedCall.callsCount)

        val WHERE_ARGS: Array<String> = arrayOf(missedCall.missedCallId.toString())
        val WHERE_CLAUSE = "$KEY_MISSED_CALLS_ID = ${missedCall.missedCallId}"

        db.update(TABLE_MISSED_CALLS, data, WHERE_CLAUSE, WHERE_ARGS)
    }

    fun deleteAll() {
        val db: SQLiteDatabase = writableDatabase
        db.beginTransaction()

        try {
            db.delete(TABLE_MISSED_CALLS, null, null)
            db.setTransactionSuccessful()
        } catch (err: Exception) {
            // CATCH
        } finally {
            db.endTransaction()
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
        const val DATABASE_VERSION: Int = 1

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
                        "$KEY_MISSED_CALLS_ID INTEGER PRIMARY KEY," +
                        "$KEY_MISSED_CALLS_PHONE_NUMBER TEXT" +
                        "$KEY_MISSED_CALLS_COUNT INTEGER" +
                    ")"
    }
}