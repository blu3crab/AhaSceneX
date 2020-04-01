///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.app.Application
import android.graphics.Rect
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
import kotlin.math.pow
import kotlin.math.sqrt

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

    var multiTouchInProgress = false
    var singleTouchInProgress = false

    var distPrev: Float = 0.0F
    var distCurr: Float = 0.0F
    var distStart: Float = 0.0F

    var x0Prev: Float = 0.0F
    var y0Prev: Float = 0.0F
    var x1Prev: Float = 0.0F
    var y1Prev: Float = 0.0F
    var x0Curr: Float = 0.0F
    var y0Curr: Float = 0.0F
    var x1Curr: Float = 0.0F
    var y1Curr: Float = 0.0F

    var deltaCurr: Float = 0.0F
    var deltaPrev: Float = 0.0F

    var scaleCurr: Float = MIN_SCALE_FACTOR
    var scalePrev: Float = MIN_SCALE_FACTOR
    var deltaScaleFactor: Float = DELTA_SCALE_FACTOR

    var centerX: Float = 0.0F
    var centerY: Float = 0.0F
    var viewportWidth: Float = 0.0F
    var viewportHeight: Float = 0.0F
    lateinit var viewportRectf: Rect
    var viewportLeftEdge = centerX - (viewportWidth/2)
    var viewportRightEdge = centerX + (viewportWidth/2)
    var viewportTopEdge = centerY - (viewportHeight/2)
    var viewportBottomEdge = centerY + (viewportHeight/2)


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

        val left = motionView.left
        val top = motionView.top
        val right = motionView.right
        val bottom = motionView.bottom
        Log.d(TAG, "MotionEvent left $left, top $top, right $right, bottom $bottom")

        val leftX = motionView.x
        val topY = motionView.y
        val width = motionView.width
        val height = motionView.height
        Log.d(TAG, "MotionEvent leftX $leftX, topY $topY, width $width, height $height")

        // get center X,Y & current visible rect
        centerX = (width/2).toFloat()
        centerY = (height/2).toFloat()
        Log.d(TAG, "MotionEvent center X,Y $centerX, $centerY")
        viewportRectf = getVRect(motionView)

        val pointerCount = motionEvent.pointerCount
        val pointerId = motionEvent.getPointerId(0)
        Log.d(TAG, "MotionEvent pointerCount = ${pointerCount}, pointerId = $pointerId")
        // multi-touch
        if (pointerCount == 2) {
            if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                Log.d(TAG, "MotionEvent multi-touch $actionString...")
                multiTouchInProgress = true
                singleTouchInProgress = false
                // set to save next on MOVE
                x0Curr = motionEvent.getX(0)
                y0Curr = motionEvent.getY(0)
                x1Curr = motionEvent.getX(1)
                y1Curr = motionEvent.getY(1)
                // determine distance between touch points
                distCurr = getDistance(x0Curr, y0Curr, x1Curr, y1Curr)
                distStart = distCurr
            }
            else if (multiTouchInProgress && (actionMasked == MotionEvent.ACTION_MOVE  ||
                actionMasked == MotionEvent.ACTION_POINTER_UP)) {
                Log.d(TAG, "MotionEvent multi-touch $actionString...")
                // save previous x,y,dist
                x0Prev = x0Curr
                y0Prev = y0Curr
                x1Prev = x1Curr
                y1Prev = y1Curr
                distPrev = distCurr
                // set current x,y
                x0Curr = motionEvent.getX(0)
                y0Curr = motionEvent.getY(0)
                x1Curr = motionEvent.getX(1)
                y1Curr = motionEvent.getY(1)
                // determine distance & delta between current/start touches
                distCurr = getDistance(x0Curr, y0Curr, x1Curr, y1Curr)
                deltaPrev = deltaCurr
                deltaCurr = distCurr - distStart
                // if delta between current/previous above thresold, scale view (attempting to smooth scaling)
                if (abs(distCurr - distPrev) > DELTA_DIFF_THRESHOLD) {
                    // if current delta is positive, add to scale factor, else subtract from scale factor
                    deltaScaleFactor = if (deltaCurr > 0) {
                        DELTA_SCALE_FACTOR
                    } else {
                        -DELTA_SCALE_FACTOR
                    }
                    scalePrev = motionView.scaleX
                    scaleCurr = deltaScaleFactor + motionView.scaleX
                    Log.d(
                        TAG,
                        "MotionEvent scale = $scaleCurr, prevScale = $scalePrev, deltaX = $deltaScaleFactor"
                    )

                    // limit scaling to prevent idiocy (zoom out to microscopic dot or zoom in to pixel)
                    if (scaleCurr < MIN_SCALE_FACTOR) {
                        scaleCurr = MIN_SCALE_FACTOR
                    } else if (scaleCurr > MAX_SCALE_FACTOR) {
                        scaleCurr = MAX_SCALE_FACTOR
                    }
                    // maintain aspect ratio
                    motionView.scaleX = scaleCurr
                    motionView.scaleY = scaleCurr
                    // reset viewport dims & visible rect for updated scale
                    viewportRectf = getVRect(motionView)
                    viewportWidth = motionView.width * motionView.scaleX
                    viewportLeftEdge = (centerX * motionView.scaleX) - (viewportWidth/2)
                    viewportRightEdge = (centerX * motionView.scaleX) + (viewportWidth/2)
                    Log.d(TAG,"MotionEvent post-scale viewportWidth $viewportWidth, " +
                            "viewportLeftEdge $viewportLeftEdge, viewportRightEdge $viewportRightEdge")
                    viewportHeight = motionView.height * motionView.scaleY
                    viewportTopEdge = (centerY * motionView.scaleY) - (viewportHeight/2)
                    viewportBottomEdge = (centerY * motionView.scaleY) + (viewportHeight/2)
                    Log.d(TAG,"MotionEvent post-scale viewportHeight $viewportHeight, " +
                            "viewportTopEdge $viewportTopEdge, viewportBottomEdge $viewportBottomEdge")
                    // if UP, mark multi-touch complete
                    if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
                        multiTouchInProgress = false
                    }
                }
            }
        }
        // single-touch
        else if (pointerCount == 1) {
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                singleTouchInProgress = true
                multiTouchInProgress = false
                Log.d(TAG, "MotionEvent single-touch $actionString...")
                // set to save next on MOVE
                x0Curr = motionEvent.getX(0)
                y0Curr = motionEvent.getY(0)
            }
            else if (singleTouchInProgress && (actionMasked == MotionEvent.ACTION_MOVE  ||
                actionMasked == MotionEvent.ACTION_UP)) {
                Log.d(TAG, "MotionEvent single-touch $actionString...")
                // save previous x,y,dist
                x0Prev = x0Curr
                y0Prev = y0Curr
                // set current x,y
                x0Curr = motionEvent.getX(0)
                y0Curr = motionEvent.getY(0)
                // determine delta between current/start touches
                val distX = x0Curr - x0Prev
                val distY = y0Curr - y0Prev
                Log.d(TAG, "MotionEvent dist x,y $distX, $distY, curr x,y $x0Curr, $y0Curr, prev x,y $x0Prev, $y0Prev")
                // if distance non-zero
                if (distX > 0 || distX < 0 || distY > 0 || distY < 0) {
                    viewportRectf = getVRect(motionView)
                    // left/right panning
                    var leftEdgeTest: Float = viewportRectf.left - distX
                    var rightEdgeTest: Float = viewportRectf.right - distX
                    var testPivotX: Float = motionView.pivotX - distX
                    Log.d(TAG,"MotionEvent leftEdgeTest $leftEdgeTest >= leftEdge $viewportLeftEdge, " +
                            "rightTest $rightEdgeTest <= rightEdge $viewportRightEdge")
                    // top/bottom panning
                    var topEdgeTest: Float = viewportRectf.top - distY
                    var bottomEdgeTest: Float = viewportRectf.bottom - distY
                    var testPivotY: Float = motionView.pivotY - distY
                    Log.d(TAG,"MotionEvent topEdgeTest $topEdgeTest >= topEdge $viewportTopEdge, " +
                            "bottomEdgeTest $bottomEdgeTest <= bottomEdge $viewportBottomEdge")

                    // test out of bounds panning
                    Log.d(TAG,"MotionEvent pre-pan pivot X ${motionView.pivotX}, Y ${motionView.pivotY}, " +
                            "textPivotX $testPivotX, textPivotY $testPivotY")
                    // motionView.pivotX = motionView.pivotX - distX
                    if (leftEdgeTest >= viewportLeftEdge && rightEdgeTest <= viewportRightEdge) {
                        Log.d(TAG, "MotionEvent left/right edge test PASS...")
                        motionView.pivotX = testPivotX
                    } else {
                        Log.d(TAG, "MotionEvent left/right edge test FAIL...")
                    }
                    //motionView.pivotY = motionView.pivotY - distY
                    if (topEdgeTest >= viewportTopEdge && bottomEdgeTest <= viewportBottomEdge) {
                        Log.d(TAG, "MotionEvent top/bottom edge test PASS...")
                        motionView.pivotY = testPivotY
                    } else {
                        Log.d(TAG, "MotionEvent top/bottom edge test FAIL...")
                    }

                    Log.d(
                        TAG,
                        "MotionEvent pan pivot X ${motionView.pivotX}, Y ${motionView.pivotY}"
                    )
                    // examine new visible rect
                    getVRect(motionView)
                    // if UP, complete single touch
                    if (actionMasked == MotionEvent.ACTION_UP) {
                        singleTouchInProgress = false
                    }
                }
            }
        }

        //Log.d(TAG, "cameraDistance ${motionView.cameraDistance}, translationX ${motionView.translationX}, translationY ${motionView.translationY}")
        //motionView.setLeftTopRightBottom(left, top, right, bottom)

    }
    private fun getVRect(motionView: View): Rect {
        // get local visible rect
        val rectf = Rect()
        motionView.getLocalVisibleRect(rectf)
        Log.d(TAG, "MotionEvent visible left ${rectf.left}, top ${rectf.top}, " +
                "right ${rectf.right}, bottom ${rectf.bottom}, " +
                "width ${rectf.width()}, height ${rectf.height()}")
        Log.d(TAG, "MotionEvent pivot X ${motionView.pivotX}, Y ${motionView.pivotY}")
        return rectf
    }
    private fun getDistance(x0: Float, y0: Float, x1: Float, y1: Float): Float {
        val dist = sqrt((x1Curr - x0Curr).pow(2) + (y1Curr - y0Curr).pow(2))
        Log.d(TAG, "MotionEvent dist ($dist) at x0, y0 = $x0, $y0 to x1, y1 = $x1, $y1")
        return dist
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