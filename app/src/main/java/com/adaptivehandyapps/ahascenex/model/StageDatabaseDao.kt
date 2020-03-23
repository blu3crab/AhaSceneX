//
// Created by MAT on 19MAR2020.
//
package com.adaptivehandyapps.ahascenex.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StageDatabaseDao {

    @Insert
    fun insert(stageModel: StageModel)

    @Update
    fun update(stageModel: StageModel)

    @Query ("SELECT * from stage_model_table WHERE tableId = :key")
    fun get(key: Long): StageModel?

    @Query("DELETE FROM stage_model_table")
    fun clear()

    @Query("SELECT * FROM stage_model_table ORDER BY tableId DESC LIMIT 1")
    fun getLast(): StageModel?

    @Query("SELECT * FROM stage_model_table ORDER BY tableId DESC")
//    fun getAll(): MutableLiveData<MutableList<StageModel>>
    fun getAll(): MutableList<StageModel>
}
