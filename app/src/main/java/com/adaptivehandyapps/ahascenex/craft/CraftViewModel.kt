///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
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
import com.adaptivehandyapps.ahascenex.formatPropModel
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
                      val context: android.content.Context,
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

    // prop list is all props for all stages
    private var _propList = MutableLiveData<MutableList<PropModel>>()
    val propList: LiveData<MutableList<PropModel>>
        get() = _propList

    // prop view maps to prop list via list index
    var propViewList: MutableList<ImageView> = mutableListOf<ImageView>()

    // current prop
    var currentPropIndex = -1

    enum class PropNicknameEnum(val nickname: String) {
        PROP_LEYLAND_T2("prop_leyland_t2_1024"),
        PROP_HOLLY_LARGE_T1("prop_holly_large_t1_1024"),
        PROP_LAUREL_SMALL_T1("prop_laurel_small_t1_1024"),
        PROP_LEYLAND_T1("prop_leyland_t1_1024"),
        PROP_FLOWER_T1("prop_flower_t1_1024")
    }

    private var resSeedId: Int = R.drawable.prop_flower_t1_1024
    private var resNickname: String = "prop_flower_t1_1024"

    ///////////////////////////////////////////////////////////////////////////
    init {
    }
    ///////////////////////////////////////////////////////////////////////////
    // PROP database
    fun getPropList(viewCraft: View) {
        uiScope.launch {
//            propModelList = getPropListFromDatabase()
//            Log.d(TAG, "getPropList size = " + propModelList.size)
            _propList.value = getPropListFromDatabase()
            Log.d(TAG, "getPropList size = " + _propList.value!!.size)
            currentPropIndex =  _propList.value!!.size - 1
            Log.d(TAG, "getPropList current prop = " + formatPropModel(_propList.value!!.get(currentPropIndex)))
            showScene(viewCraft)
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
    // draw scene, label & existing props
    fun showScene(viewCraft: View) {
        // set label
        val editTextSceneLabel = viewCraft.findViewById<EditText>(R.id.edittext_scene_label)
        editTextSceneLabel.setText(_stageModel.value!!.label)
        // set image view
        val imgView = viewCraft.findViewById<ImageView>(R.id.imageview_scene)
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
            if (stageModel.value!!.sceneScale != 0.0F) {
                // adjust scale, pivot while maintaining aspect ratio
                imgView.scaleX = stageModel.value!!.sceneScale
                imgView.scaleY = stageModel.value!!.sceneScale
                imgView.pivotX = stageModel.value!!.sceneX
                imgView.pivotY = stageModel.value!!.sceneY
            }
        }
        catch (ex : Exception) {
            Log.e("BindingAdapter", "scenex Glide exception! " + ex.localizedMessage)
        }
        //getPropList() // async process!
        Log.d(TAG, "propModelList size " + (propList.value?.size ?: "undefined"))
        // for each prop in prop list
        _propList.value?.let {
            for (propModel in _propList.value!!.listIterator()) {
                Log.d(TAG, "showScene for " + formatPropModel(propModel, false))
                Log.d(TAG, "propModel.stageId == stageModel.tableId?" + propModel.stageId + "==" + stageModel.value!!.tableId)
                // if prop on this stage
                if (propModel.stageId == stageModel.value!!.tableId) {
                    // add prop view
                    addPropView(viewCraft, false)
                    Log.d(TAG, "showScene after addPropView for " + formatPropModel(propModel, false))
                }
            }
        }
    }
    // add prop to stage view
    fun addPropView(stageView: View, addToDB: Boolean) {
        //fun addProp(view: View, context: android.content.Context) {
        val craftLayout = stageView?.findViewById<ConstraintLayout>(R.id.craft_layout)

        resSeedId = cycleProp(resSeedId)
        resNickname = syncPropNickname(resSeedId)
        val dimensions = BitmapFactory.Options()
        dimensions.inJustDecodeBounds = true
        val mBitmap = BitmapFactory.decodeResource(context.resources, resSeedId, dimensions)
        val height = dimensions.outHeight
        val width = dimensions.outWidth

        var propView: ImageView

        propView = ImageView(context)

        craftLayout.addView(propView)

        propView.layoutParams.height = height/2
        propView.layoutParams.width = width/2
        propView.x = 520F   // TODO: center in stage dynamically
        propView.y = 620F
        //propView.setBackgroundColor(Color.MAGENTA)
        propView.setImageResource(resSeedId)

        // add listener
        propView.setOnTouchListener {
                motionView: View, motionEvent: MotionEvent ->
            craftTouch.onTouch(motionView, motionEvent)
            // TODO: capture results of scene touch motion events
            // capture results of prop touch motion events
            updatePropModelSceneTouch(motionView)

            true
        }

        // add prop to DB
        if (addToDB) addPropModel(propView, resSeedId)

        // add to prop view to prop view list
        propViewList.add(propView)

        // map prop to view setting as current prop
        mapPropViewToPropModel(propView)
    }

    fun cycleProp(resIdSeed: Int): Int {
        var resId = resIdSeed
        when (resId) {
            R.drawable.prop_flower_t1_1024 -> resId = R.drawable.prop_leyland_t2_1024
            R.drawable.prop_leyland_t2_1024 -> resId = R.drawable.prop_holly_large_t1_1024
            R.drawable.prop_holly_large_t1_1024 -> resId = R.drawable.prop_laurel_small_t1_1024
            R.drawable.prop_laurel_small_t1_1024 -> resId = R.drawable.prop_leyland_t1_1024
            else -> resId = R.drawable.prop_flower_t1_1024
        }
        return resId
    }
    fun syncPropNickname(resId: Int): String {
        var resNickname = PropNicknameEnum.PROP_FLOWER_T1
        when (resId) {
            R.drawable.prop_leyland_t2_1024 -> resNickname = PropNicknameEnum.PROP_LEYLAND_T2
            R.drawable.prop_holly_large_t1_1024 -> resNickname = PropNicknameEnum.PROP_HOLLY_LARGE_T1
            R.drawable.prop_laurel_small_t1_1024 -> resNickname = PropNicknameEnum.PROP_LAUREL_SMALL_T1
            R.drawable.prop_leyland_t1_1024 -> resNickname = PropNicknameEnum.PROP_LEYLAND_T1
            else -> resNickname = PropNicknameEnum.PROP_FLOWER_T1
        }
        return resNickname.toString()
    }

    ///////////////////////////////////////////////////////////////////////////
    // PROP Database
    // generate prop model & associate with prop view
    // add prop model attributes
    fun addPropModel(propView: ImageView, resId: Int) {
        // capture prop model attributes
        var propModel = PropModel()
//      "propModel id# " + propModel.nickname + " = " + propModel.label + ", type " + propModel.type +
//      "\n res id " + propModel.propResId + ", stage id " + propModel.stageId +
//      "\n prop scale = " + propModel.propScale + ", prop x/y = " + propModel.propX + "/" + propModel.propY
        propModel.stageId = stageModel.value!!.tableId
        propModel.propResId = resId
        propModel.nickname = resNickname
        //propModel.type = PropType.PROP_TYPE.toString()
        //propModel.label = "nada"
        propModel.propScale = propView.scaleX
        propModel.propX = propView.x
        propModel.propY = propView.y

        // insert prop model in DB
        insertPropModelDatabase(propModel)
    }
    // add to prop model database
    fun insertPropModelDatabase(propModel: PropModel) {
        Log.d(TAG, "insertPropModelDatabase ")
        uiScope.launch {
            propModel?.let {
                insert(it)
                Log.d(TAG, "insertPropModelDatabase DB update for " + formatPropModel(propModel, false))
            }
        }
    }
    private suspend fun insert(propModel: PropModel) {
        withContext(Dispatchers.IO) {
            propDatabase.insert(propModel)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // update prop model scale & x,y
    fun updatePropModelSceneTouch(motionView: View) {
        var propScalePivot = craftTouch.propScalePivot
        var propModel = mapPropViewToPropModel(motionView)
        propModel.propScale = propScalePivot.scale
        propModel.propX = propScalePivot.x
        propModel.propY = propScalePivot.y
        updatePropModelDatabase(propModel)
        Log.d(TAG, "updatePropModelSceneTouch-> " + formatPropModel(propModel))
    }
    // map prop view to prop model
    fun mapPropViewToPropModel(motionView: View): PropModel {
        var i = 0
        for (propView in propViewList) {
            if (propView == motionView) {
                Log.d(TAG, "mapPropViewToPropModel propView found at position " + i)
                currentPropIndex = i
                return propList.value!!.get(i)
            }
            i += 1
        }
        return propList.value!!.get(i)
    }
    // update to prop model database
    fun updatePropModelDatabase(propModel: PropModel) {
        Log.d(TAG, "updatePropModelDatabase ")
        uiScope.launch {
            propModel?.let {
                update(it)
                Log.d(TAG, "updatePropModelDatabase DB update for " + formatPropModel(propModel, false))
            }
        }
    }
    private suspend fun update(propModel: PropModel) {
        withContext(Dispatchers.IO) {
            propDatabase.update(propModel)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // remove current prop & redraw
    fun removeCurrentProp() {
        if (currentPropIndex > -1) {
            var propModel = _propList.value!!.get(currentPropIndex)
            // remove prop in DB
            deletePropModelDatabase(propModel)
            // remove prop in view list
            propViewList.removeAt(currentPropIndex)

            // remove prop in prop list
            _propList.value!!.removeAt(currentPropIndex)

            // indicate removal
            currentPropIndex -= 1
        }
        else Log.d(TAG, "removeCurrentProp undefined...")
    }
    // discard props from database for stage model
    fun deletePropModelDatabase(propModel: PropModel) {
        Log.d(TAG, "deletePropModelDatabase ")
        uiScope.launch {
            Log.d(TAG,"deletePropModelDatabase " + formatPropModel(propModel,false))
            deleteProp(propModel)
        }
    }
    // discard props from database for stage model
    fun deletePropModelDatabaseForStage() {
        Log.d(TAG, "deleteIdFromStageModelDatabase ")
        uiScope.launch {
            stageModel?.let {
                for (propModel in _propList.value!!.listIterator()) {
                    if (propModel.stageId == stageModel.value!!.tableId) {
                        Log.d(TAG,"deleteStageIdFromPropModelDatabase " + formatPropModel(propModel,false))
                        deleteProp(propModel)
                    }
                }
            }
        }
    }
    private suspend fun deleteProp(propModel: PropModel) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "delete " + propModel.tableId)
            propDatabase.deletePropModel(propModel.tableId)
        }
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
    // update stage model scene scale & pivot
    fun updateStageModelSceneTouch() {
        var sceneScalePosition = craftTouch.sceneScalePivot
        stageModel.value!!.sceneScale = sceneScalePosition.scale
        stageModel.value!!.sceneX = sceneScalePosition.x
        stageModel.value!!.sceneY = sceneScalePosition.y
        Log.d(TAG, "updateStageModelSceneTouch-> " + formatStageModel(stageModel.value))
    }
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
    // STAGE Database
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