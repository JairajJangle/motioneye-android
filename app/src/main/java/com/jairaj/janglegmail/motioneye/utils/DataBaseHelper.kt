package com.jairaj.janglegmail.motioneye.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject
import java.util.*

class DataBaseHelper internal constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    private val logTAG = DataBaseHelper::class.java.name

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table " + TABLE_NAME
                    + "(" +
                    "$ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$LABEL TEXT NOT NULL UNIQUE," +
                    "$URL TEXT," +
                    "$PORT TEXT," +
                    "$DRIVE TEXT," +
                    "$PREVIEW TEXT" +
                    "$CRED TEXT" +
                    "$SORT_INDEX NUMBER" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $PREVIEW TEXT")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $CRED TEXT")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $SORT_INDEX NUMBER")
        }
    }

    fun insertNewColumn() {
        val db = this.writableDatabase
        if (!existsColumnInTable(
                db,
                TABLE_NAME,
                PREVIEW
            )
        ) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $PREVIEW TEXT  DEFAULT 0")
        }
        if (!existsColumnInTable(
                db,
                TABLE_NAME,
                CRED
            )
        ) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $CRED TEXT  DEFAULT ''")
        }
        if (!existsColumnInTable(
                db,
                TABLE_NAME,
                SORT_INDEX
            )
        ) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $SORT_INDEX NUMBER  DEFAULT -1")
        }
    }

    fun insertData(
        label: String?,
        url: String?,
        port: String?,
        drive: String?,
        prev: String?,
        sortIndex: Int?,

        // Get Cred Encrypted JSON Str from getEncryptedCredJSONStr(...)
        credEncryptedJSONStr: String?
    ): Boolean {
        insertNewColumn()
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LABEL, label)
        contentValues.put(URL, url)
        contentValues.put(PORT, port)
        contentValues.put(DRIVE, drive)
        contentValues.put(PREVIEW, prev)
        contentValues.put(SORT_INDEX, sortIndex)
        contentValues.put(CRED, credEncryptedJSONStr)
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun getHighestSortIndex(): Int {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT MAX($SORT_INDEX) FROM $TABLE_NAME", null)
        var highestValue = 0

        if (cursor.moveToFirst()) {
            highestValue = cursor.getInt(0)
        }
        cursor.close()

        return highestValue
    }

    @Suppress("SameParameterValue")
    private fun existsColumnInTable(
        inDatabase: SQLiteDatabase,
        inTable: String,
        columnToCheck: String
    ): Boolean {
        var mCursor: Cursor? = null
        return try {
            // Query 1 row
            mCursor = inDatabase.rawQuery("SELECT * FROM $inTable LIMIT 0", null)

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            mCursor.getColumnIndex(columnToCheck) != -1
        } catch (e: Exception) {
            // Something went wrong. Missing the database? The table?
            Log.d(
                logTAG,
                "When checking whether a column exists in the table, an error occurred: $e"
            )
            false
        } finally {
            mCursor?.close()
        }
    }

    fun hasLabel(label: String): Boolean {
        val db = this.writableDatabase
        val selectString = "SELECT * FROM $TABLE_NAME WHERE $LABEL =?"

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        val cursor = db.rawQuery(selectString, arrayOf(label))
        var hasObject = false
        if (cursor.moveToFirst()) {
            hasObject = true

            // region if you had multiple records to check for, use this region.
            var count = 0
            while (cursor.moveToNext()) {
                count++
            }
            // here, count is records found
            Log.d(logTAG, String.format("%d records found", count))

            // endregion
        }
        cursor.close() // Don't forget to close your cursor
        db.close() //AND your Database!
        return hasObject
    }

    val allData: Cursor
        get() {
            val db = this.writableDatabase
            return db.rawQuery(
                "select * from $TABLE_NAME ORDER BY $SORT_INDEX ASC;",
                null
            )
        }

    fun urlFromLabel(searchedLabel: String): String {
        val db = this.writableDatabase
        var cursor: Cursor? = null
        var url = ""
        return try {
            cursor =
                db.rawQuery(
                    "select $URL from $TABLE_NAME where $LABEL=?",
                    arrayOf(searchedLabel + "")
                )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                url = cursor.getString(cursor.getColumnIndexOrThrow(URL))
            }
            url
        } catch (e: Exception) {
            Log.e(logTAG, "Error while reading URL from Database: $e")
            ""
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun portFromLabel(searchedLabel: String): String {
        val db = this.writableDatabase
        var cursor: Cursor? = null
        var port: String? = ""
        return try {
            cursor =
                db.rawQuery(
                    "select $PORT from $TABLE_NAME where $LABEL=?",
                    arrayOf(searchedLabel + "")
                )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                port = cursor.getString(cursor.getColumnIndexOrThrow(PORT))
            }
            port ?: ""
        } catch (e: Exception) {
            Log.e(logTAG, "Error while reading Port from Database: $e")
            ""
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun driveFromLabel(searchedLabel: String): String {
        val db = this.writableDatabase
        var cursor: Cursor? = null
        var driveLink: String? = ""
        return try {
            cursor = db.rawQuery("select * from $TABLE_NAME", null)
            if (cursor.getColumnIndex(DRIVE) != -1) {
                cursor = db.rawQuery(
                    "select $DRIVE from $TABLE_NAME where $LABEL=?",
                    arrayOf(searchedLabel + "")
                )
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    driveLink = cursor.getString(cursor.getColumnIndexOrThrow(DRIVE))
                }
                driveLink ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(logTAG, "Error while reading Drive URL from Database: $e")
            ""
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun prevStatFromLabel(searchedLabel: String): String {
        val db = this.writableDatabase
        var cursor: Cursor? = null
        var prev: String? = ""
        return try {
            cursor =
                db.rawQuery(
                    "select $PREVIEW from $TABLE_NAME where $LABEL=?",
                    arrayOf(searchedLabel + "")
                )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                prev = cursor.getString(cursor.getColumnIndexOrThrow(PREVIEW))
            }
            prev ?: ""
        } catch (e: Exception) {
            Log.e(logTAG, "Error while reading Preview Status from Database: $e")
            ""
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun sortIndexFromLabel(searchedLabel: String): Int {
        val db = this.writableDatabase
        var cursor: Cursor? = null
        var sortIndex: Int? = -1
        return try {
            cursor =
                db.rawQuery(
                    "select $SORT_INDEX from $TABLE_NAME where $LABEL=?",
                    arrayOf(searchedLabel + "")
                )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                sortIndex = cursor.getInt(cursor.getColumnIndexOrThrow(SORT_INDEX))
            }
            sortIndex ?: -1
        } catch (e: Exception) {
            Log.e(logTAG, "Error while reading Sort Index from Database: $e")
            -1
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun updateSortIndexForLabel(targetLabel: String, sortIndex: Int) {
        val sql = "UPDATE $TABLE_NAME SET SORT_INDEX = ? WHERE LABEL = ?"
        val db = this.writableDatabase
        db.execSQL(sql, arrayOf(sortIndex, targetLabel))
    }

    fun credJSONFromLabel(searchedLabel: String): String {
        val db = this.writableDatabase
        var cursor: Cursor? = null
        var cred = ""
        return try {
            cursor =
                db.rawQuery(
                    "select $CRED from $TABLE_NAME where $LABEL=?",
                    arrayOf(searchedLabel + "")
                )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                cred = cursor.getString(cursor.getColumnIndexOrThrow(CRED))
            }
            cred
        } catch (e: Exception) {
            Log.e(logTAG, "Error while reading Credential JSON from Database: $e")
            ""
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun getEncryptedCredJSONStr(username: String, password: String): String {
        val encryptedDataHandler = EncryptedDataHandler()
        val encryptedUsername = encryptedDataHandler.getEncryptedData(username)
        val encryptedPassword = encryptedDataHandler.getEncryptedData(password)

        return "{" +

                // Username ByteArray Pair
                "   \"user\": {" +
                "       \"1\": \"${
                    Base64.getEncoder().encodeToString(encryptedUsername.first)
                }\"," +
                "       \"2\": \"${
                    Base64.getEncoder().encodeToString(encryptedUsername.second)
                }\"" +
                "   }," +

                // Password ByteArray Pair
                "   \"pass\": {" +
                "       \"1\": \"${
                    Base64.getEncoder().encodeToString(encryptedPassword.first)
                }\"," +
                "       \"2\": \"${
                    Base64.getEncoder().encodeToString(encryptedPassword.second)
                }\"" +
                "   }" +

                "}"
    }

    fun getDecryptedCred(encryptedCredJSONStr: String): Pair<String, String> {
        if (encryptedCredJSONStr.isEmpty()) {
            // TODO: Set username and password as blank
            Log.i("TAG", "Stored JSON is empty, keeping username and password as blank")
            return Pair("", "")
        } else {
            val encryptedDataHandler = EncryptedDataHandler()
            val storedJSON = JSONObject(encryptedCredJSONStr)

            val extractedUserNamePair =
                Pair(
                    Base64.getDecoder()
                        .decode(storedJSON.getJSONObject("user").get("1").toString()),
                    Base64.getDecoder()
                        .decode(storedJSON.getJSONObject("user").get("2").toString()),
                )

            val extractedPasswordPair =
                Pair(
                    Base64.getDecoder()
                        .decode(storedJSON.getJSONObject("pass").get("1").toString()),
                    Base64.getDecoder()
                        .decode(storedJSON.getJSONObject("pass").get("2").toString()),
                )

            val decryptedUserName =
                encryptedDataHandler.getDecryptedData(
                    extractedUserNamePair.first,
                    extractedUserNamePair.second
                )
            val decryptedPassword =
                encryptedDataHandler.getDecryptedData(
                    extractedPasswordPair.first,
                    extractedPasswordPair.second
                )

            return Pair(decryptedUserName, decryptedPassword)
        }
    }

    fun updateData(
        old_label: String,
        new_label: String?,
        url: String?,
        port: String?,
        drive: String?,
        sortIndex: Int?,

        // Get Cred Encrypted JSON Str from getEncryptedCredJSONStr(...)
        credEncryptedJSONStr: String?
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LABEL, new_label)
        contentValues.put(URL, url)
        contentValues.put(PORT, port)
        contentValues.put(DRIVE, drive)
        contentValues.put(SORT_INDEX, sortIndex)
        contentValues.put(CRED, credEncryptedJSONStr)
        db.update(TABLE_NAME, contentValues, "$LABEL = ?", arrayOf(old_label))
        return true
    }

    fun updatePrevStat(key_label: String, prev_stat: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(PREVIEW, prev_stat)
        db.update(TABLE_NAME, contentValues, "$LABEL = ?", arrayOf(key_label))
        return true
    }

    fun deleteData(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$LABEL = ?", arrayOf(id))
    }

    companion object {
        private const val DATABASE_NAME = "Devices.db"
        private const val TABLE_NAME = "device_detail_table"

        // Column names in sequence 
        private const val ID = "ID"
        private const val LABEL = "LABEL"
        private const val URL = "URL"
        private const val PORT = "PORT"
        private const val DRIVE = "DRIVE"
        private const val PREVIEW = "PREV"
        private const val CRED = "CRED"
        private const val SORT_INDEX = "SORT_INDEX"
    }
}