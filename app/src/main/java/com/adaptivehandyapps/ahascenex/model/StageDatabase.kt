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

@Database(entities = [StageModel::class], version = 2, exportSchema = false)
abstract class StageDatabase : RoomDatabase() {

    abstract val stageDatabaseDao: StageDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: StageDatabase? = null

        fun getInstance(context: Context): StageDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            StageDatabase::class.java,
                            "stage_database"
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
