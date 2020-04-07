///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.formatStageModel
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.*

///////////////////////////////////////////////////////////////////////////
//class CraftViewModel : ViewModel()
class CraftViewModel (val database: StageDatabaseDao,
                      application: Application) : AndroidViewModel(application)
{
    private val TAG = "CraftViewModel"

    // coroutines
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _stageModel = MutableLiveData<StageModel>()
    val stageModel: LiveData<StageModel>
        get() = _stageModel

    private var stageModelCheckPoint: StageModel = StageModel()

    // touch handler for scene zoom & pan
    var craftTouch: CraftTouch = CraftTouch()

    ///////////////////////////////////////////////////////////////////////////
    init {
    }
    ///////////////////////////////////////////////////////////////////////////
    // frag initialization sets stage model after unbundling args
    fun loadStageModel(updatedStageModel: StageModel) {
        // retain stage model checkpoint unless stage model is undefined
        stageModel.value?.let {stageModelCheckPoint = stageModel.value!!} ?: run {stageModelCheckPoint = updatedStageModel}
        // update stage model
        _stageModel.value = updatedStageModel
        Log.d(TAG, "updated stageModel id# " + _stageModel.value!!.nickname + " = " + _stageModel.value!!.label +
                ", type " + _stageModel.value!!.type + ", uri " + _stageModel.value!!.sceneSrcUrl)
    }
    ///////////////////////////////////////////////////////////////////////////
    //fun showScene(view: View, imgView: ImageView, imgUri: Uri) {
    fun showScene(view: View) {
        // set label
        val editTextSceneLabel = view.findViewById<EditText>(R.id.edittext_scene_label)
        editTextSceneLabel.setText(_stageModel.value!!.label)
        // set image view
        val imgView = view.findViewById<ImageView>(R.id.imageview_scene)
        val imgUrl = stageModel.value!!.sceneSrcUrl
        val imgUri = imgUrl!!.toUri()

        try {
            Glide.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .into(imgView)
        }
        catch (ex : Exception) {
            Log.e("BindingAdapter", "scenex Glide exception! " + ex.localizedMessage)
        }
    }
//    fun showProp(view: View) {
//        // set image view
//        val imgView = view.findViewById<ImageView>(R.id.imageview_prop)
//        val imgUrl = stageModel.value!!.sceneSrcUrl
//        val imgUri = imgUrl!!.toUri()
//
//        try {
//            Glide.with(imgView.context)
//                .load(imgUri)
//                .apply(
//                    RequestOptions()
//                        .placeholder(R.drawable.loading_animation)
//                        .error(R.drawable.ic_broken_image)
//                )
//                .into(imgView)
//        }
//        catch (ex : Exception) {
//            Log.e("BindingAdapter", "scenex Glide exception! " + ex.localizedMessage)
//        }
//    }

    ///////////////////////////////////////////////////////////////////////////
    // update stage model label
    fun updateStageModelLabel(label: String) {
        stageModelCheckPoint.label = stageModel.value!!.label
        stageModel.value!!.label = label
    }
    // undo stage model changes - label, props
    fun undoStageModel() {
        Log.d(TAG, "undoStageModel replaces current " + formatStageModel(stageModel.value, false))
        _stageModel.value = stageModelCheckPoint
        Log.d(TAG, "undoStageModel with checkpoint " + formatStageModel(stageModel.value, false))
    }
    ///////////////////////////////////////////////////////////////////////////
    // update stage model database
    fun updateStageModelDatabase() {
        Log.d(TAG, "addStageModel ")
        uiScope.launch {
            stageModel.value?.let {
                update(it)
                Log.d(TAG, "updateStageModel DB update " + formatStageModel(stageModel.value, false))
            }
        }
    }
    private suspend fun update(stageModel: StageModel) {
        withContext(Dispatchers.IO) {
            database.update(stageModel)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // discard scene from stage model database
    fun deleteIdFromStageModelDatabase() {
        Log.d(TAG, "deleteIdFromStageModelDatabase ")
        uiScope.launch {
            stageModel?.let {
                Log.d(TAG, "deleteIdFromStageModelDatabase " + formatStageModel(stageModel.value, false))
                delete(stageModel.value!!)
            }
        }
    }
    private suspend fun delete(stageModel: StageModel) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "delete " + stageModel.tableId)
            database.deleteStageModel(stageModel.tableId)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
}