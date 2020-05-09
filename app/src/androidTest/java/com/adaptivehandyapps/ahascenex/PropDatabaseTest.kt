//
// Created by MAT on 3/19/2020.
//
package com.adaptivehandyapps.ahascenex

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adaptivehandyapps.ahascenex.model.*
import org.junit.After
import org.junit.Assert.*
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
        propModel.label = "test0001"
        propDao.insert(propModel)
        val tonight = propDao.getLast()
        assertEquals(tonight?.label, "test0001")
    }

    @Test
    @Throws(Exception::class)
    fun insertUpdateAndGetLast() {
        val propModel = PropModel()
        propModel.nickname = "test0001"
        propDao.insert(propModel)

        val insertModel = propDao.getLast()
        assertNotNull(insertModel)
        assertEquals(insertModel?.nickname, "test0001")

        insertModel?.nickname = "test0002"

        propDao.update(insertModel!!)
        val updateModel = propDao.getLast()
        assertNotNull(updateModel)
        assertEquals(updateModel?.nickname, "test0002")
    }

    @Test
    @Throws(Exception::class)
    fun insertDeleteAndGetLast() {
        val propModel = PropModel()
        propModel.label = "test0001"
        propDao.insert(propModel)

        var insertModel = propDao.getLast()
        assertNotNull(insertModel)
        assertEquals(insertModel?.label, "test0001")

        insertModel!!.tableId = 0
        insertModel!!.label = "test0002"
        propDao.insert(insertModel)
        assertNotNull(insertModel)
        val insertSecondModel = propDao.getLast()
        assertNotNull(insertSecondModel)
        assertEquals(insertSecondModel?.label, "test0002")

        propDao.deletePropModelByKey(insertSecondModel!!.tableId)

        val firstModel = propDao.getLast()
        assertNotNull(firstModel)
        assertEquals(firstModel?.label, "test0001")

    }
}