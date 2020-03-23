package com.adaptivehandyapps.ahascenex.craft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class CraftViewModelFactory () : ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CraftViewModel::class.java)) {
            return CraftViewModel() as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}