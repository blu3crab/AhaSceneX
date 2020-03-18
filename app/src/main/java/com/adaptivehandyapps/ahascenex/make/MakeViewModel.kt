package com.adaptivehandyapps.ahascenex.make

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adaptivehandyapps.ahascenex.model.StageModel

class MakeViewModel : ViewModel() {
    private val TAG = "MakeViewModel"

    private val _stageModel = MutableLiveData<StageModel>()
    val status: LiveData<StageModel>
        get() = _stageModel

}