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

    private val _stageList = MutableLiveData<MutableList<StageModel>>()
    val stageList: LiveData<MutableList<StageModel>>
        get() = _stageList

    init {
        Log.d(TAG, "scenex init...")
        // empty stage list
        emptyStageList()
        _status.value = StageListStatus.READY
        // TEST: set EMPTY, replace empty initial list element with first pick
        //_status.value = StageListStatus.EMPTY
        //_stageList.value = ArrayList()
        //setTestStageList()
        //getStageList(StageType.ALL_TYPE)
    }

    private fun setTestStageList() {
        // TEST: clear initial list
        // stage list is empty
        //_status.value = StageListStatus.EMPTY
        //_stageList.value = ArrayList()
        // add test stage list entries
        var stageModel = StageModel()
        stageModel.id = stageModelId.toString()
        stageModel.type = StageType.ICON_TYPE.value
        //stageModel.sceneSrcUrl = "content://com.google.android.apps.photos.contentprovider/0/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F7889/ORIGINAL/NONE/image%2Fjpeg/1140719721"
        stageModel.sceneSrcUrl = "add_to_queue-24px.svg"
        stageModel.label = "dummy test image"
        // TEST: initialize 1 list element, replace entire list
        //var testList = MutableList<StageModel>(1) { stageModel }
        //_stageList.value = testList   // ok
        // TEST: directly add test element to list
        _stageList.value?.add(stageModel)   // ok - add empty
        // TEST: add to list using actual setter
        //addStageModel(stageModel)   // ok
        //_status.value = StageListStatus.LOCAL
    }

    fun addStageModel(stageModel: StageModel): Boolean {
        var inserted = false
        if (_status.value == StageListStatus.EMPTY) {
            //emptyStageList()
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
        // TEST: if list not null
        //val list = _stageList
        //list?.let {
        // iterate through stagelist
        for (stageModel in _stageList.value!!.listIterator()) {
            Log.d(TAG, "stageModel id# " + stageModel.id +" = " + stageModel.label)
        }
        // mark list ready
        _status.value = StageListStatus.READY
        return inserted
    }
    // clear stagelist
    private fun emptyStageList() {
        Log.d(TAG, "stageModel empty...")
        _status.value = StageListStatus.EMPTY
        _stageList.value = ArrayList()
    }
    // get next stagemodel id
    private fun getNextStageModelId(): Int {
        stageModelId += 1
        return stageModelId
    }
    // get stagelist size
    fun getStageListSize(): Int {
        val size = _stageList.value!!.size
        return size
    }
}