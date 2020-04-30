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
import com.adaptivehandyapps.ahascenex.model.*
import kotlinx.coroutines.*

//enum class StageStatus { EMPTY, SCENE, LABEL, PROP, READY }
enum class StageListStatus { EMPTY, LAUNCH, LOCAL, READY }

///////////////////////////////////////////////////////////////////////////
//class StageViewModel : ViewModel() {
class StageViewModel ( //val database: StageDatabaseDao,
    val stageDatabase: StageDatabaseDao,
    val propDatabase: PropDatabaseDao,
    application: Application) : AndroidViewModel(application)
{
    private val TAG = "StageViewModel"

    // coroutines
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

//    var activeStageModel = MutableLiveData<StageModel?>()
    var activeStageListInx: Int = -1
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

//        // get first stage model from DB
//        getLastStageModel()
        // get stage list from DB
        getStageList()
    }
    ///////////////////////////////////////////////////////////////////////////
    // public helpers
    // set active stage list index
    fun setActiveStageListInx(stageModel: StageModel): Int {
        // clear active stage index
        activeStageListInx = -1
        // find stage in stage list
        var i = _stageList.value?.indexOf(stageModel)
        if (i != null) {
            activeStageListInx = i
        }
        Log.d(TAG, "setActiveStageListInx sets active stage index = $activeStageListInx")
        return activeStageListInx
    }
    // get active stage
    fun getActiveStage(): StageModel? {
        var stageModel: StageModel? = null
        if (activeStageListInx >= 0 && _stageList.value?.size!! > activeStageListInx) {
            stageModel = _stageList.value!!.get(activeStageListInx)
            Log.d(TAG,"getActiveStage " + formatStageModel(stageModel,false))
        }
        return stageModel
    }
    // get active stage
    fun removeActiveStageFromList(): Boolean {
        if (activeStageListInx >= 0 && _stageList.value?.size!! > activeStageListInx) {
            _stageList.value!!.removeAt(activeStageListInx)
            Log.d(TAG,"removeActiveStageFromList at index " + activeStageListInx)
            activeStageListInx = -1
            return true
        }
        return false
    }
    ///////////////////////////////////////////////////////////////////////////
    // coroutines: cancel all coroutines when the ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    ///////////////////////////////////////////////////////////////////////////
    // coroutines: prop model database
    ///////////////////////////////////////////////////////////////////////////
    // discard all props from database
    fun clearPropDatabase() {
        Log.d(TAG, "deletePropDatabase ")
        uiScope.launch {
            Log.d(TAG,"deletePropDatabase...")
            clearPropModelDatabase()
        }
    }
    private suspend fun clearPropModelDatabase() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "clearAllProp...")
            propDatabase.clear()
        }
    }

    // discard props from database for stage model
    fun deletePropForStage(stageModel: StageModel) {
        Log.d(TAG, "deletePropModelDatabaseForStage ")
        uiScope.launch {
            stageModel?.let {
                Log.d(TAG,"deletePropModelDatabaseForStage " + formatStageModel(stageModel,true))
                deleteStageProp(stageModel.tableId)
            }
        }
    }
    private suspend fun deleteStageProp(stageTableId: Long) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "deleteProp for stageId " + stageTableId)
            propDatabase.deletePropModelByStageId(stageTableId)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // coroutines: stage model database
    ///////////////////////////////////////////////////////////////////////////
    // delete stage from stage model database
    fun deleteStageFromDatabase(stageModel: StageModel) {
        Log.d(TAG, "removeStageFromDatabase ")
        uiScope.launch {
            stageModel?.let {
                // delete stage from DB
                Log.d(TAG, "deleteStageFromDatabase " + formatStageModel(stageModel, false))
                delete(stageModel!!)
            }
        }
    }
    private suspend fun delete(stageModel: StageModel) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "delete " + stageModel.tableId)
            stageDatabase.deleteStageModel(stageModel.tableId)
        }
    }

    // clear all stages in database
    fun clearStageDatabase() {
        uiScope.launch {
            // empty stage list
            emptyStageList()
            // clear DB
            clearStageModelDatabase()
            Log.d(TAG, "clearStageListFromDatabase...")
        }
    }
    private suspend fun clearStageModelDatabase() {
        return withContext(Dispatchers.IO) {
            stageDatabase.clear()
        }
    }

    // get all stages in database
    private fun getStageList() {
        uiScope.launch {
            _stageList.value = getStageListFromDatabase()
            Log.d(TAG, "initializeStageList size = " + _stageList.value?.size)
            _stageList.value?.size?.let {
                if (_stageList.value!!.size > 0) {
                    // set active stage index to last stage
                    activeStageListInx = _stageList.value!!.size.minus(1)
                    Log.d(TAG, "initializeStageList sets active stage inx " + activeStageListInx)
                    // get the nickname (one up number) of the last stage to bump when new stage is added
                    val stageModel = _stageList.value!!.get(_stageList.value!!.size.minus(1))
                    stageModelNickname = stageModel.nickname.toInt() + 1
                    Log.d(TAG, "initializeStageList stageModelNickname = $stageModelNickname")
                }
            }
        }
    }
    private suspend fun getStageListFromDatabase(): MutableList<StageModel> {
        return withContext(Dispatchers.IO) {
            var list: MutableList<StageModel> = stageDatabase.getAll()
            if (list == null){
                list = mutableListOf<StageModel>()
            }
            list
        }
    }

//    // get last stage model
//    private fun getLastStageModel() {
//        // set the last stage model as the active stage
//        uiScope.launch {
//            var stageModel = getLastStageModelFromDatabase()
//            Log.d(TAG, "getLastStageModel " + formatStageModel(stageModel, true))
//        }
//    }
//    private suspend fun getLastStageModelFromDatabase(): StageModel? {
//        return withContext(Dispatchers.IO) {
//            var stageModel = stageDatabase.getLast()
//            stageModel
//        }
//    }

    // add stage model by adding to stage list and inserting in database
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
                Log.d(TAG, "addStageModel DB insert " + formatStageModel(stageModel, false))
                // set active stage index to last stage
                activeStageListInx = _stageList.value!!.size.minus(1)
                Log.d(TAG, "addStageModel sets active stage inx " + activeStageListInx)
            }
        }
        // log resulting stage list
        _stageList.value?.let {
            // iterate through stagelist
            for (stageModel in _stageList.value!!.listIterator()) {
                Log.d(TAG, formatStageModel(stageModel))    // terse mode parameter unnecessary, defaults to true
            }
            // mark list ready
            _status.value = StageListStatus.READY
        } ?: run {
            _status.value = StageListStatus.EMPTY
        }
        return inserted
    }
    private suspend fun insert(stageModel: StageModel) {
        withContext(Dispatchers.IO) {
            stageDatabase.insert(stageModel)
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
    ///////////////////////////////////////////////////////////////////////////
    // TEST experiments
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
}