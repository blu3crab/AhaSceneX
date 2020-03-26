///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 28FEB2020.
//
package com.adaptivehandyapps.ahascenex.stage

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adaptivehandyapps.ahascenex.formatStageModel
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.adaptivehandyapps.ahascenex.model.StageType
import kotlinx.coroutines.*

//enum class StageStatus { EMPTY, SCENE, LABEL, PROP, READY }
enum class StageListStatus { EMPTY, LAUNCH, LOCAL, READY }

///////////////////////////////////////////////////////////////////////////
//class StageViewModel : ViewModel() {
class StageViewModel ( val database: StageDatabaseDao,
                       application: Application) : AndroidViewModel(application)
{
    private val TAG = "StageViewModel"

    // coroutines
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var activeStageModel = MutableLiveData<StageModel?>()
    // model
    private var stageModelNickname = 999

    private val _status = MutableLiveData<StageListStatus>()
    val status: LiveData<StageListStatus>
        get() = _status

    private var _stageList = MutableLiveData<MutableList<StageModel>>()
    val stageList: LiveData<MutableList<StageModel>>
        get() = _stageList

    ///////////////////////////////////////////////////////////////////////////
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

        // get first stage model from DB
        initializeStageModel()
        // get stage list from DB
        initializeStageList()
    }
    ///////////////////////////////////////////////////////////////////////////
    // coroutines: stage model
    fun clearStageList() {
        uiScope.launch {
            clearStageListFromDatabase()
            Log.d(TAG, "clearStageListFromDatabase...")
        }
    }

    private suspend fun clearStageListFromDatabase() {
        return withContext(Dispatchers.IO) {
            database.clear()
        }
    }
    private fun initializeStageList() {
        uiScope.launch {
            _stageList.value = getStageListFromDatabase()
            Log.d(TAG, "initializeStageList size = " + _stageList.value?.size)
        }
    }

    private suspend fun getStageListFromDatabase(): MutableList<StageModel> {
        return withContext(Dispatchers.IO) {
            var list: MutableList<StageModel> = database.getAll()
            if (list == null){
                list = mutableListOf<StageModel>()
            }
            list
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // coroutines: stage model
    private fun initializeStageModel() {
        uiScope.launch {
            activeStageModel.value = getStageModelFromDatabase()
            Log.d(TAG, "initializeStageModel " + formatStageModel(activeStageModel.value, true))
        }
    }

    private suspend fun getStageModelFromDatabase(): StageModel? {
        return withContext(Dispatchers.IO) {
            var stageModel = database.getLast()
//            if (stageModel?.endTimeMilli != stageModel?.startTimeMilli) {
//                stageModel = null
//            }
            stageModel
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // coroutines: cancel all coroutines when the ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    ///////////////////////////////////////////////////////////////////////////
    private fun setTestStageList() {
        // TEST: clear initial list
        // stage list is empty
        //_status.value = StageListStatus.EMPTY
        //_stageList.value = ArrayList()
        // add test stage list entries
        var stageModel = StageModel()
        stageModel.nickname = stageModelNickname.toString()
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
            stageModel.nickname = getNextStageModelNickname().toString()
            Log.d(TAG, "addStageModel " + formatStageModel(stageModel, false))
            _stageList.value?.add(stageModel)
            inserted = true
        }
        if (inserted) {
            Log.d(TAG, "addStageModel ")
            uiScope.launch {
                insert(stageModel)
                activeStageModel.value = getStageModelFromDatabase()
                Log.d(TAG, "addStageModel DB insert " + formatStageModel(activeStageModel.value, false))
            }

        }
        // TEST: if list not null
        //val list = _stageList
        //list?.let {
        // iterate through stagelist
        for (stageModel in _stageList.value!!.listIterator()) {
            Log.d(TAG, formatStageModel(stageModel))    // terse mode parameter unnecessary, defaults to true
        }
        // mark list ready
        _status.value = StageListStatus.READY
        return inserted
    }
    private suspend fun insert(stageModel: StageModel) {
        withContext(Dispatchers.IO) {
            database.insert(stageModel)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // private helpers
    // clear stagelist
    private fun emptyStageList() {
        Log.d(TAG, "stageModel empty...")
        _status.value = StageListStatus.EMPTY
        _stageList.value = ArrayList()
    }
    // get next stagemodel id
    private fun getNextStageModelNickname(): Int {
        stageModelNickname += 1
        return stageModelNickname
    }
    // get stagelist size
    fun getStageListSize(): Int {
        val size = _stageList.value!!.size
        return size
    }
//    fun toString(stageModel: StageModel?, terse: Boolean = false): String {
//        stageModel?.let {
//            if (terse) {
//                return "stageModel nickname# " + stageModel.nickname + " label " + stageModel.label
//            }
//            return "stageModel id# " + stageModel.nickname + " = " + stageModel.label +
//                    ", type " + stageModel.type + ", uri " + stageModel.sceneSrcUrl
//        }
//        return "stageModel NULL... "
//    }

}