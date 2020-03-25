package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import java.lang.IllegalArgumentException

class CraftViewModelFactory (
    private val dataSource: StageDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory
{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CraftViewModel::class.java)) {
            return CraftViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}