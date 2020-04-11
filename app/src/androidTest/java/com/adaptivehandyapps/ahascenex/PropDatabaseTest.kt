//
// Created by MAT on 3/19/2020.
//
package com.adaptivehandyapps.ahascenex

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adaptivehandyapps.ahascenex.model.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PropDatabaseTest {

    private lateinit var propDao: PropDatabaseDao
    private lateinit var db: PropDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, PropDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        propDao = db.propDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetLast() {
        val propModel = PropModel()
        propDao.insert(propModel)
        val tonight = propDao.getLast()
        assertEquals(propModel?.label, "nada")
    }

    @Test
    @Throws(Exception::class)
    fun insertUpdateAndGetLast() {
        val propModel = PropModel()
        propModel.nickname = "1234"
        propDao.insert(propModel)
        val lastStageModel = propDao.getLast()
        assertEquals(propModel?.nickname, "1234")
    }
}