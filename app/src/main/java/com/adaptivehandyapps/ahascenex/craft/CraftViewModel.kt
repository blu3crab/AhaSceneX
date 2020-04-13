///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.formatStageModel
import com.adaptivehandyapps.ahascenex.model.PropDatabaseDao
import com.adaptivehandyapps.ahascenex.model.PropModel
import com.adaptivehandyapps.ahascenex.model.StageDatabaseDao
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.*

///////////////////////////////////////////////////////////////////////////
//class CraftViewModel : ViewModel()
class CraftViewModel (val stageDatabase: StageDatabaseDao,
                      val propDatabase: PropDatabaseDao,
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

    var propModelList: MutableList<PropModel> = mutableListOf<PropModel>()

    private var resSeedId: Int = R.drawable.prop_flower_t1_1024

    ///////////////////////////////////////////////////////////////////////////
    init {
        getPropList()
        Log.d(TAG, "propModelList size " + propModelList.size)
    }
    ///////////////////////////////////////////////////////////////////////////
    private fun getPropList() {
        uiScope.launch {
            propModelList = getPropListFromDatabase()
            Log.d(TAG, "getPropList size = " + propModelList.size)
        }
    }

    private suspend fun getPropListFromDatabase(): MutableList<PropModel> {
        return withContext(Dispatchers.IO) {
            var list: MutableList<PropModel> = propDatabase.getAll()
            if (list == null){
                list = mutableListOf<PropModel>()
            }
            list
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // frag initialization sets stage model after unbundling args
    fun loadStageModel(updatedStageModel: StageModel) {
        // retain stage model checkpoint unless stage model is undefined
        stageModel.value?.let {stageModelCheckPoint = stageModel.value!!} ?: run {stageModelCheckPoint = updatedStageModel}
        // update stage model
        _stageModel.value = updatedStageModel
        Log.d(TAG, "loadStageModel-> " + formatStageModel(stageModel.value))
    }
    ///////////////////////////////////////////////////////////////////////////
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
            // adjust scale, pivot while maintaining aspect ratio
            imgView.scaleX = stageModel.value!!.sceneScale
            imgView.scaleY = stageModel.value!!.sceneScale
            imgView.pivotX = stageModel.value!!.sceneX
            imgView.pivotY = stageModel.value!!.sceneY
        }
        catch (ex : Exception) {
            Log.e("BindingAdapter", "scenex Glide exception! " + ex.localizedMessage)
        }

    }
    fun addProp(view: View, context: android.content.Context) {
        val craftLayout = view?.findViewById<ConstraintLayout>(R.id.craft_layout)

        resSeedId = cycleProp(resSeedId)
        val dimensions = BitmapFactory.Options()
        dimensions.inJustDecodeBounds = true
//        val mBitmap = BitmapFactory.decodeResource(resources, resSeedId, dimensions)
        val mBitmap = BitmapFactory.decodeResource(context.resources, resSeedId, dimensions)
        val height = dimensions.outHeight
        val width = dimensions.outWidth

        var propView: ImageView

        propView = ImageView(context)

        craftLayout.addView(propView)

        propView.layoutParams.height = height/2
        propView.layoutParams.width = width/2
        propView.x = 520F
        propView.y = 620F
        //propView.setBackgroundColor(Color.MAGENTA)
        propView.setImageResource(resSeedId)

        // add listener
        propView.setOnTouchListener {
                motionView: View, motionEvent: MotionEvent ->
            craftTouch.onTouch(motionView, motionEvent)
            // TODO: capture results of scene touch motion events

            true
        }

        // TODO: add prop to DB
    }

    fun cycleProp(resIdSeed: Int): Int {
        var resId = resIdSeed
        when (resId) {
            R.drawable.prop_flower_t1_1024 -> resId = R.drawable.prop_holly_large_t1_1024
            R.drawable.prop_holly_large_t1_1024 -> resId = R.drawable.prop_laurel_small_t1_1024
            R.drawable.prop_laurel_small_t1_1024 -> resId = R.drawable.prop_leyland_t1_1024
            R.drawable.prop_leyland_t1_1024 -> resId = R.drawable.prop_leyland_t2_1024
            else -> resId = R.drawable.prop_flower_t1_1024
        }
        return resId
    }
    ///////////////////////////////////////////////////////////////////////////
    fun saveStageModel(view: View) {
        // save label
        val editText = view?.findViewById<EditText>(R.id.edittext_scene_label)
        val label = editText?.text
        Log.d(TAG, "edittext_scene_label " + label + "...")
        //craftViewModel.stageModel.value!!.label = label.toString()
        updateStageModelLabel(label.toString())
        // update database
        updateStageModelDatabase()
    }

    ///////////////////////////////////////////////////////////////////////////
    // update stage model label
    fun updateStageModelSceneTouch() {
        var sceneScalePosition = craftTouch.sceneScalePivot
        stageModel.value!!.sceneScale = sceneScalePosition.scale
        stageModel.value!!.sceneX = sceneScalePosition.x
        stageModel.value!!.sceneY = sceneScalePosition.y
        Log.d(TAG, "updateStageModelSceneTouch-> " + formatStageModel(stageModel.value))
    }
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
        Log.d(TAG, "updateStageModelDatabase ")
        uiScope.launch {
            stageModel.value?.let {
                update(it)
                Log.d(TAG, "updateStageModel DB update " + formatStageModel(stageModel.value, false))
            }
        }
    }
    private suspend fun update(stageModel: StageModel) {
        withContext(Dispatchers.IO) {
            stageDatabase.update(stageModel)
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
            stageDatabase.deleteStageModel(stageModel.tableId)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
}