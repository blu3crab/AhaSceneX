///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 3/19/2020.
//
package com.adaptivehandyapps.ahascenex.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PropModel::class], version = 3, exportSchema = false)
abstract class PropDatabase : RoomDatabase() {

    abstract val propDatabaseDao: PropDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: PropDatabase? = null

        fun getInstance(context: Context): PropDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            PropDatabase::class.java,
                            "prop_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
