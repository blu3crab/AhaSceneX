//
// Created by MAT on 3/19/2020.
//
package com.adaptivehandyapps.ahascenex

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import com.adaptivehandyapps.ahascenex.model.StageModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SleepDatabaseTest {

    private lateinit var stageDao: StageDatabaseDao
    private lateinit var db: StageDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, StageDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        stageDao = db.stageDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetLast() {
        val stageModel = StageModel()
        stageDao.insert(stageModel)
        val tonight = stageDao.getLast()
        assertEquals(stageModel?.label, "nada")
    }

    @Test
    @Throws(Exception::class)
    fun insertUpdateAndGetLast() {
        val stageModel = StageModel()
        stageModel.nickname = "1234"
        stageDao.insert(stageModel)
        val lastStageModel = stageDao.getLast()
        assertEquals(stageModel?.nickname, "1234")
    }
}