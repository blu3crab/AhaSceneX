///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// CraftTouch: touch handler for scene zoom & pan
//
// Created by MAT on 01APR2020.
//
package com.adaptivehandyapps.ahascenex.craft

import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.model.StageModel
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

///////////////////////////////////////////////////////////////////////////
// CraftTouch: touch handler for scene zoom & pan
class CraftTouch {
    private val TAG = "CraftTouch"

    // zoom support
    val MIN_SCALE_FACTOR = 1.0F
    val MAX_SCALE_FACTOR = 16.0F
    val DELTA_SCALE_FACTOR = 0.2F
    val DELTA_DIFF_THRESHOLD = 4.0F
    //val DELTA_DIFF_THRESHOLD = 8.0F

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

    // pan support
    var centerX: Float = 0.0F
    var centerY: Float = 0.0F
    var viewportWidth: Float = 0.0F
    var viewportHeight: Float = 0.0F
    lateinit var viewportRectf: Rect
    var viewportLeftEdge = centerX - (viewportWidth/2)
    var viewportRightEdge = centerX + (viewportWidth/2)
    var viewportTopEdge = centerY - (viewportHeight/2)
    var viewportBottomEdge = centerY + (viewportHeight/2)

    class ScalePivot (scale: Float, x: Float, y:Float){
        // scene scale & pivot X,Y
        var scale: Float = scale
        var x: Float = x
        var y: Float = y
    }

    var sceneScalePivot = ScalePivot(MIN_SCALE_FACTOR, 0.0F, 0.0F)

    ///////////////////////////////////////////////////////////////////////////
    init {
    }

    fun setSceneScalePivot(stageModel: StageModel) {
        sceneScalePivot.scale = stageModel.sceneScale
        sceneScalePivot.x = stageModel.sceneX
        sceneScalePivot.y = stageModel.sceneY
    }
    ///////////////////////////////////////////////////////////////////////////
    // touch handler
    fun onTouch(motionView: View, motionEvent: MotionEvent) {

        Log.d(TAG, "MotionEvent onTouch view id ${motionView.id}")
        var sceneMotion = true
        if (!motionView.id.equals(R.id.imageview_scene)) {
            sceneMotion = false
            Log.d(TAG, "MotionEvent onTouch for PROP view")
        }
        Log.d(TAG, "MotionEvent onTouch action(raw) $motionEvent")
        val actionMasked = motionEvent.actionMasked
        val actionString = getActionMaskedString(actionMasked)
        Log.d(TAG, "MotionEvent action($actionMasked)= $actionString")

        // log view dimensions
        //        val left = motionView.left
        //        val top = motionView.top
        //        val right = motionView.right
        //        val bottom = motionView.bottom
        //        Log.d(TAG, "MotionEvent left $left, top $top, right $right, bottom $bottom")
        Log.d(TAG, "MotionEvent leftX ${motionView.x}, topY ${motionView.y}, width ${motionView.width}, height ${motionView.height}")

        // get center X,Y & current visible rect
        centerX = (motionView.width/2).toFloat()
        centerY = (motionView.height/2).toFloat()
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
                    if (sceneMotion) {
                        if (scaleCurr < MIN_SCALE_FACTOR) {
                            scaleCurr = MIN_SCALE_FACTOR
                        } else if (scaleCurr > MAX_SCALE_FACTOR) {
                            scaleCurr = MAX_SCALE_FACTOR
                        }
                    }
                    // maintain aspect ratio
                    motionView.scaleX = scaleCurr
                    motionView.scaleY = scaleCurr
                    // if scene is focus, retain scene scale
                    if (sceneMotion) sceneScalePivot.scale = scaleCurr

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
                    if (sceneMotion) {
                        viewportRectf = getVRect(motionView)
                        // left/right panning
                        var leftEdgeTest: Float = viewportRectf.left - distX
                        var rightEdgeTest: Float = viewportRectf.right - distX
                        var testPivotX: Float = motionView.pivotX - distX
                        Log.d(
                            TAG,
                            "MotionEvent leftEdgeTest $leftEdgeTest >= leftEdge $viewportLeftEdge, " +
                                    "rightTest $rightEdgeTest <= rightEdge $viewportRightEdge"
                        )
                        // top/bottom panning
                        var topEdgeTest: Float = viewportRectf.top - distY
                        var bottomEdgeTest: Float = viewportRectf.bottom - distY
                        var testPivotY: Float = motionView.pivotY - distY
                        Log.d(
                            TAG,
                            "MotionEvent topEdgeTest $topEdgeTest >= topEdge $viewportTopEdge, " +
                                    "bottomEdgeTest $bottomEdgeTest <= bottomEdge $viewportBottomEdge"
                        )

                        // test out of bounds panning
                        Log.d(
                            TAG,
                            "MotionEvent pre-pan pivot X ${motionView.pivotX}, Y ${motionView.pivotY}, " +
                                    "textPivotX $testPivotX, textPivotY $testPivotY"
                        )
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
                        // if scene is focus, retain pivot X/Y
                        sceneScalePivot.x = motionView.pivotX
                        sceneScalePivot.y = motionView.pivotY

                        Log.d(
                            TAG,
                            "MotionEvent pan scene pivot X ${motionView.pivotX}, Y ${motionView.pivotY}"
                        )
                        // examine new visible rect
                        getVRect(motionView)
                    }
                    else {
//                        motionView.x = distX
//                        motionView.y = distY
                        var deltaX = 0.0F
                        if (distX < 0) deltaX = -DELTA_DIFF_THRESHOLD
                        else if (distX > 0) deltaX = DELTA_DIFF_THRESHOLD
                        var deltaY = 0.0F
                        if (distY < 0) deltaY = -DELTA_DIFF_THRESHOLD
                        else if (distY > 0) deltaY = DELTA_DIFF_THRESHOLD
                        Log.d(TAG,"MotionEvent pre-pan prop X ${motionView.x}, Y ${motionView.y}, delta x,y $deltaX, $deltaY")
                        motionView.x += deltaX
                        motionView.y += deltaY
                        Log.d(TAG,"MotionEvent pan prop X ${motionView.x}, Y ${motionView.y}")
                    }
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