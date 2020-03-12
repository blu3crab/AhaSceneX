package com.adaptivehandyapps.ahascenex.stage

import androidx.lifecycle.ViewModel

class StageViewModel : ViewModel() {
    private val TAG = "StageViewModel"

//    var _sceneList = MutableLiveData<List<String>>()
//
//    val getSceneList: LiveData<List<String>>
//        get() = _sceneList
//    var sceneList : ArrayList<String> = ArrayList()
//
//    fun addScene(sceneName : String) {
//        sceneList.add(sceneName)
//    }

    val sceneList = mutableListOf("scene #1", "scene #2", "scene #3", "scene #4")

}