package com.adaptivehandyapps.ahascenex.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class MakeViewModelFactory () : ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MakeViewModel::class.java)) {
            return MakeViewModel() as T
        }
        throw IllegalArgumentException("Oops! unknown ViewModel class...")
    }

}