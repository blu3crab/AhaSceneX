package com.adaptivehandyapps.ahascenex.craft

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adaptivehandyapps.ahascenex.model.StageModel

class CraftViewModel : ViewModel() {
    private val TAG = "MakeViewModel"

    private val _stageModel = MutableLiveData<StageModel>()
    val stageModel: LiveData<StageModel>
        get() = _stageModel

    fun setStageModel(updateStageModel: StageModel) {
        _stageModel.value = updateStageModel
        Log.d(TAG, "updated stageModel id# " + _stageModel.value!!.nickname + " = " + _stageModel.value!!.label +
                ", type " + _stageModel.value!!.type + ", uri " + _stageModel.value!!.sceneSrcUrl)
    }
}