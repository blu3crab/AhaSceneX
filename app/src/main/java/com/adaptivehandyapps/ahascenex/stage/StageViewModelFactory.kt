package com.adaptivehandyapps.ahascenex.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class StageViewModelFactory () : ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StageViewModel::class.java)) {
            return StageViewModel() as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}