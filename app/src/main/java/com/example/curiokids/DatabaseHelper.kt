package com.example.curiokids

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper(private val context: Context) {
    private val dbName = "pdf_storage.db"
    private val dbPath: String
        get() = context.getDatabasePath(dbName).path

    fun openDatabase(): SQLiteDatabase {
        copyDatabaseIfNeeded()
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun copyDatabaseIfNeeded() {
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(dbName).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

