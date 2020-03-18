package com.adaptivehandyapps.ahascenex.stage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.adaptivehandyapps.ahascenex.model.StageType

//enum class StageStatus { EMPTY, SCENE, LABEL, PROP, READY }
enum class StageListStatus { EMPTY, LAUNCH, LOCAL, READY }

class StageViewModel : ViewModel() {
    private val TAG = "StageViewModel"

    private var stageModelId = 999

    private val _status = MutableLiveData<StageListStatus>()
    val status: LiveData<StageListStatus>
        get() = _status

    //private val _stageList = MutableLiveData<List<StageModel>>()
    //val stageList: LiveData<List<StageModel>>
    //    get() = _stageList
    private val _stageList = MutableLiveData<MutableList<StageModel>>()
    val stageList: LiveData<MutableList<StageModel>>
        get() = _stageList

    init {
        Log.d(TAG, "scenex init...")
        // empty stage list
        emptyStageList()
        _status.value = StageListStatus.READY

//        _status.value = StageListStatus.EMPTY
//        _stageList.value = ArrayList()
        //setTestStageList()
        //getStageList(StageType.ALL_TYPE)
    }
//    content://com.google.android.apps.photos.contentprovider/0/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F7889/ORIGINAL/NONE/image%2Fjpeg/1140719721

    private fun setTestStageList() {
//        // stage list is empty
//        _status.value = StageListStatus.EMPTY
//        _stageList.value = ArrayList()
        // add test stage list entries
        var stageModel = StageModel()
        stageModel.id = stageModelId.toString()
        stageModel.type = StageType.ICON_TYPE.value
//        stageModel.sceneSrcUrl = "content://com.google.android.apps.photos.contentprovider/0/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F7889/ORIGINAL/NONE/image%2Fjpeg/1140719721"
        stageModel.sceneSrcUrl = "add_to_queue-24px.svg"
        stageModel.label = "dummy test image"

        //var testList = MutableList<StageModel>(1) { stageModel }
        //_stageList.value = testList   // ok
        _stageList.value?.add(stageModel)   // ok - add empty
        //addStageModel(stageModel)   // ok
//        _status.value = StageListStatus.LOCAL

    }

//    /**
//     * Sets the value of the status LiveData to the Mars API status.
//     */
//    private fun getStageList(filter: StageType) {
//        // stage list is empty
//        _status.value = StageListStatus.EMPTY
//        _stageList.value = ArrayList()
//        // add test stage list entries
//        var stageModel = StageModel()
//        stageModel.id = "1000"
//        stageModel.type = StageType.ICON_TYPE.value
//        stageModel.sceneSrcUrl = "add_to_queue-24px.svg"
//        stageModel.label = "Add Scene from Photos"
//
//        var testList = List<StageModel>(1) { stageModel }
//
//        _stageList.value = testList
//    }

    fun addStageModel(stageModel: StageModel): Boolean {
        var inserted = false
        if (_status.value == StageListStatus.EMPTY) {
//            emptyStageList()
            // set list ops
            //_stageList.value!!.set(0, stageModel)
            _stageList.value!![0] = stageModel
            //_stageList.value = _stageList.value
        }
        else {
            stageModel.id = getNextStageModelId().toString()
            Log.d(TAG, "add stageModel id# " + stageModel.id + " = " + stageModel.label)
            _stageList.value?.add(stageModel)
            inserted = true
        }

//        val list = _stageList
//        list?.let {
        for (stageModel in _stageList.value!!.listIterator()) {
            Log.d(TAG, "stageModel id# " + stageModel.id +" = " + stageModel.label)
        }
//        }
        _status.value = StageListStatus.READY
        return inserted
    }

    private fun emptyStageList() {
        Log.d(TAG, "stageModel empty...")
        _status.value = StageListStatus.EMPTY
        _stageList.value = ArrayList()
    }

    private fun getNextStageModelId(): Int {
        stageModelId += 1
        return stageModelId
    }

    fun getStageListSize(): Int {
        val size = _stageList.value!!.size
        return size
    }
}