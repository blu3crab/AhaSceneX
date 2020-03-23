package com.adaptivehandyapps.ahascenex.stage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import java.lang.IllegalArgumentException

class StageViewModelFactory (
    private val dataSource: StageDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory
{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StageViewModel::class.java)) {
            return StageViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}