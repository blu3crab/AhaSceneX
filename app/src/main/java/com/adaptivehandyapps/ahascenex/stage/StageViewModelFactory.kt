///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 28FEB2020
//
package com.adaptivehandyapps.ahascenex.stage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adaptivehandyapps.ahascenex.model.PropDatabaseDao
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import java.lang.IllegalArgumentException

class StageViewModelFactory (
//    private val dataSource: StageDatabaseDao,
    private val stageDataSource: StageDatabaseDao,
    private val propDataSource: PropDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory
{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StageViewModel::class.java)) {
            return StageViewModel(stageDataSource, propDataSource, application) as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}