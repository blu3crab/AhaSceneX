///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adaptivehandyapps.ahascenex.model.PropDatabaseDao
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import java.lang.IllegalArgumentException

class CraftViewModelFactory (
    private val stageDataSource: StageDatabaseDao,
    private val propDataSource: PropDatabaseDao,
    private val context: android.content.Context,
    private val application: Application) : ViewModelProvider.Factory
{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CraftViewModel::class.java)) {
            return CraftViewModel(stageDataSource, propDataSource, context, application) as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}