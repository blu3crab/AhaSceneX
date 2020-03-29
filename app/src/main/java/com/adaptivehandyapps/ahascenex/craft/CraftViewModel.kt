///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import kotlin.math.abs
import kotlin.math.max

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

    // touch handler
    val MIN_SCALE_FACTOR = 1.0F
    val MAX_SCALE_FACTOR = 16.0F
    val DELTA_SCALE_FACTOR = 0.2F
    val DELTA_DIFF_THRESHOLD = 4.0F

    var x0Start: Float = 0.0F
    var y0Start: Float = 0.0F
    var x1Start: Float = 0.0F
    var y1Start: Float = 0.0F
    var deltaStart: Float = 0.0F

    var x0Prev: Float = 0.0F
    var y0Prev: Float = 0.0F
    var x1Prev: Float = 0.0F
    var y1Prev: Float = 0.0F
    var x0Next: Float = 0.0F
    var y0Next: Float = 0.0F
    var x1Next: Float = 0.0F
    var y1Next: Float = 0.0F

    var scale: Float = MIN_SCALE_FACTOR
    var prevScale: Float = MIN_SCALE_FACTOR
    var deltaScaleFactor: Float = DELTA_SCALE_FACTOR

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
    // touch handler
    fun onTouch(motionView: View, motionEvent: MotionEvent) {

        Log.d(TAG, "MotionEvent" + motionEvent.toString())
        val actionMasked = motionEvent.actionMasked
        Log.d(TAG, "MotionEvent action(raw) = $motionEvent.actionMasked")
        val actionString = getActionMaskedString(actionMasked)
        Log.d(TAG, "MotionEvent action($actionMasked)= $actionString")

        val pointerCount = motionEvent.pointerCount
        val pointerId = motionEvent.getPointerId(0)
        Log.d(TAG, "MotionEvent pointerCount = ${pointerCount}, pointerId = $pointerId")
        // multi-touch
        if (pointerCount == 2) {
            if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                // set starting x,y
                x0Start = motionEvent.getX(0)
                y0Start = motionEvent.getY(0)
                x1Start = motionEvent.getX(1)
                y1Start = motionEvent.getY(1)
                deltaStart = abs(x1Start - x0Start)

                x0Next = motionEvent.getX(0)
                y0Next = motionEvent.getY(0)
                x1Next = motionEvent.getX(1)
                y1Next = motionEvent.getY(1)
                Log.d(TAG, "MotionEvent $actionString delta ($deltaStart) at x0, y0 = $x0Next, $y0Next to x1, y1 = $x1Next, $y1Next")
            }
            else if (actionMasked == MotionEvent.ACTION_MOVE  ||
                actionMasked == MotionEvent.ACTION_POINTER_UP) {
            // TODO: smooth pinch/zoom by handling MOVE
//            else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
                // save previous x,y
                x0Prev = x0Next
                y0Prev = y0Next
                x1Prev = x1Next
                y1Prev = y1Next
                // set next x,y
                x0Next = motionEvent.getX(0)
                y0Next = motionEvent.getY(0)
                x1Next = motionEvent.getX(1)
                y1Next = motionEvent.getY(1)
//                val deltaPrev = abs(x1Prev - x0Prev)
//                val deltaNext = abs(x1Next - x0Next)
//                val deltaNext = max(abs(x1Next - x0Start), abs(x0Next - x1Start))
                val deltaNext = abs(x1Next - x0Start)

                Log.d(TAG, "MotionEvent $actionString delta ($deltaNext) at x0, y0 = $x0Next, $y0Next to x1, y1 = $x1Next, $y1Next")
                // if delta above thresold, scale view (attempt to smooth scaling)
                if (deltaNext > DELTA_DIFF_THRESHOLD) {
                    // ugh!
                    deltaScaleFactor = if (deltaNext > deltaStart) {
                        DELTA_SCALE_FACTOR
                    } else {
                        -DELTA_SCALE_FACTOR
                    }
                    prevScale = motionView.scaleX
                    scale = deltaScaleFactor + motionView.scaleX
                    Log.d(
                        TAG,
                        "MotionEvent scale = $scale, prevScale = $prevScale, deltaX = $deltaScaleFactor"
                    )

                    // limit scaling to prevent idiocy
                    if (scale < MIN_SCALE_FACTOR) {
                        scale = MIN_SCALE_FACTOR
                    } else if (scale > MAX_SCALE_FACTOR) {
                        scale = MAX_SCALE_FACTOR
                    }
                    motionView.scaleX = scale
                    motionView.scaleY = scale
                }
            }
        }
    }
    private fun getActionMaskedString(actionMasked: Int): String {
        var actionString: String
        when (actionMasked)
        {
            MotionEvent.ACTION_DOWN -> actionString = "DOWN"
            MotionEvent.ACTION_UP -> actionString = "UP"
            MotionEvent.ACTION_POINTER_DOWN -> actionString = "PNTR DOWN"
            MotionEvent.ACTION_POINTER_UP -> actionString = "PNTR UP"
            MotionEvent.ACTION_MOVE -> actionString = "MOVE"
            else -> actionString = ""
        }
        return actionString
    }
    ///////////////////////////////////////////////////////////////////////////
}