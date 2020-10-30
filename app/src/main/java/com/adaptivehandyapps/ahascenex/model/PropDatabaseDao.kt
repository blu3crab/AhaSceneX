///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 19MAR2020.
//
package com.adaptivehandyapps.ahascenex.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface PropDatabaseDao {

    @Insert
    fun insert(propModel: PropModel)

    @Update
    fun update(propModel: PropModel)

    @Query ("DELETE FROM prop_model_table WHERE tableId = :key")
    fun deletePropModelByKey(key: Long)

    @Query ("DELETE FROM prop_model_table WHERE stage_nickname = :key")
    fun deletePropModelByStageId(key: String)

    @Query ("SELECT * from prop_model_table WHERE tableId = :key")
    fun get(key: Long): PropModel?

    @Query("DELETE FROM prop_model_table")
    fun clear()

    @Query("SELECT * FROM prop_model_table ORDER BY tableId DESC LIMIT 1")
    fun getLast(): PropModel?

    @Query("SELECT * FROM prop_model_table ORDER BY tableId DESC")
    fun getAll(): MutableList<PropModel>
}
