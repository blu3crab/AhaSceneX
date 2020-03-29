///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020
//
package com.adaptivehandyapps.ahascenex.craft

//import androidx.core.content.PermissionChecker.checkSelfPermission
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentCraftBinding
import com.adaptivehandyapps.ahascenex.formatStageModel
import com.adaptivehandyapps.ahascenex.model.StageDatabase

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CraftFragment : Fragment() {
    private val TAG = "CraftFragment"

    private lateinit var craftViewModel: CraftViewModel
    private lateinit var binding: FragmentCraftBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_craft, container, false)

        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)

        // Room: application & database for viewmodel instantiation
        val application = requireNotNull(this.activity).application

        val dataSource = StageDatabase.getInstance(application).stageDatabaseDao

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = CraftViewModelFactory(dataSource, application)
        craftViewModel = ViewModelProvider(this, viewModelFactory).get(CraftViewModel::class.java)

        // bind view model
        binding.viewModel = craftViewModel
        // extract stage model from args
        val args = CraftFragmentArgs.fromBundle(arguments!!)
        val stageModel = args.stageModel
        // load view model with stage model
        craftViewModel.loadStageModel(stageModel)
        // SafeArgs iteration where stage model data passed element by element
        //stageModel.id = args.stageModelId
        //stageModel.label = args.stageModelLabel
        //stageModel.type = args.stageModelType
        //stageModel.sceneSrcUrl = args.stageModelSceneSrcUrl

        //Toast.makeText(context, "testInt: ${args.testInt}, testString: ${args.testString}", Toast.LENGTH_LONG).show()
        //Toast.makeText(context, "testInt: ${args.testInt}, testString: ${args.testString}", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "stageModel id#  ${stageModel.nickname} = ${stageModel.label}", Toast.LENGTH_LONG).show()
        //Log.d(TAG, "stageModel id# " + stageModel.nickname + " = " + stageModel.label + ", type " + stageModel.type + ", uri " + stageModel.sceneSrcUrl)
        Log.d(TAG, "onCreateView SafeArgs-> " + formatStageModel(stageModel))

        return binding.root

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // button_save_stage updates DB then navigates back to stage fragment
        view.findViewById<Button>(R.id.button_save_stage).setOnClickListener {
            // save stage model
            saveStageModel()
            // navigate back to stage frag
            findNavController().navigate(R.id.action_CraftFragment_to_StageFragment)
        }
        // button_undo_stage restores stage model to previous state
        view.findViewById<Button>(R.id.button_undo_stage).setOnClickListener {
            // undo stage model
            craftViewModel.undoStageModel()
            craftViewModel.showScene(view)
        }
        // button_discard_stage removes the current stage model from the database
        view.findViewById<Button>(R.id.button_discard_stage).setOnClickListener {
            // undo stage model
            craftViewModel.deleteIdFromStageModelDatabase()
            // navigate back to stage frag
            findNavController().navigate(R.id.action_CraftFragment_to_StageFragment)
        }
        // imageview_scene touch interactions
        view.findViewById<ImageView>(R.id.imageview_scene).setOnTouchListener {
                motionView: View, motionEvent: MotionEvent ->
            craftViewModel.onTouch(motionView, motionEvent)

            true    // pass touch on
        }

        // show scene
        craftViewModel.showScene(view)
    }
//    ///////////////////////////////////////////////////////////////////////////
//    private fun onTouch(motionView: View, motionEvent: MotionEvent) {
//        val MIN_SCALE_FACTOR = 1.0F
//        val MAX_SCALE_FACTOR = 16.0F
//        val DELTA_SCALE_FACTOR = 0.2F
//
//        var x0: Float = 0.0F
//        var y0: Float = 0.0F
//        var x1: Float = 0.0F
//        var y1: Float = 0.0F
//
//        var scale: Float = MIN_SCALE_FACTOR
//        var prevScaleX: Float = MIN_SCALE_FACTOR
//        var deltaX: Float = MIN_SCALE_FACTOR
//        var prevDeltaX: Float = MIN_SCALE_FACTOR
//
//        Log.d(TAG, "MotionEvent" + motionEvent.toString())
//        val actionMasked = motionEvent.actionMasked
//        Log.d(TAG, "MotionEvent action(raw) = $motionEvent.actionMasked")
//        val actionString = getActionMaskedString(actionMasked)
//        Log.d(TAG, "MotionEvent action($actionMasked)= $actionString")
//
//        val pointerCount = motionEvent.pointerCount
//        val pointerId = motionEvent.getPointerId(0)
//        Log.d(TAG, "MotionEvent pointerCount = ${pointerCount}, pointerId = $pointerId")
//        // multi-touch
//        if (pointerCount == 2) {
//            if (actionMasked == MotionEvent.ACTION_POINTER_DOWN  ||
//                actionMasked == MotionEvent.ACTION_MOVE  ||
//                actionMasked == MotionEvent.ACTION_POINTER_UP) {
//                x0 = motionEvent.getX(0)
//                y0 = motionEvent.getY(0)
//                x1 = motionEvent.getX(1)
//                y1 = motionEvent.getY(1)
//                Log.d(TAG, "MotionEvent $actionString at x0, y0 = $x0, $y0 to x1, y1 = $x1, $y1")
//
//                prevScaleX = motionView.scaleX
////                prevDeltaX = deltaX
////                deltaX = (x0 / x1)
//                deltaX = if (x1 > x0) {
//                    DELTA_SCALE_FACTOR
//                } else {
//                    -DELTA_SCALE_FACTOR
//                }
//                scale = deltaX + motionView.scaleX
////                if (deltaX > prevDeltaX) {
////                    scale = deltaX + motionView.scaleX
////                }
////                else {
////                    scale = deltaX - motionView.scaleX
////                }
//                Log.d(TAG, "MotionEvent scale = $scale, prevScale = $prevScaleX, deltaX = $deltaX")
//                // limit scaling to prevent idiocy
//                if (scale < MIN_SCALE_FACTOR) {
//                    scale = MIN_SCALE_FACTOR
//                }
//                else if (scale > MAX_SCALE_FACTOR) {
//                    scale = MAX_SCALE_FACTOR
//                }
//                motionView.scaleX = scale
//                motionView.scaleY = scale
//            }
//        }
//    }
//    private fun getActionMaskedString(actionMasked: Int): String {
//        var actionString: String
//        when (actionMasked)
//        {
//            MotionEvent.ACTION_DOWN -> actionString = "DOWN"
//            MotionEvent.ACTION_UP -> actionString = "UP"
//            MotionEvent.ACTION_POINTER_DOWN -> actionString = "PNTR DOWN"
//            MotionEvent.ACTION_POINTER_UP -> actionString = "PNTR UP"
//            MotionEvent.ACTION_MOVE -> actionString = "MOVE"
//            else -> actionString = ""
//        }
//        return actionString
//    }
    ///////////////////////////////////////////////////////////////////////////
    fun saveStageModel() {
        // save label
        val editText = view?.findViewById<EditText>(R.id.edittext_scene_label)
        val label = editText?.text
        Log.d(TAG, "edittext_scene_label " + label + "...")
        //craftViewModel.stageModel.value!!.label = label.toString()
        craftViewModel.updateStageModelLabel(label.toString())
        // update database
        craftViewModel.updateStageModelDatabase()
    }
    ///////////////////////////////////////////////////////////////////////////
    override fun onStart() {
        Log.i(TAG, "onStart invoked...")
        super.onStart()
    }
    override fun onResume() {
        Log.i(TAG, "onResume invoked...")
        super.onResume()
    }
    override fun onPause() {
        Log.i(TAG, "onPause invoked...")
        super.onPause()
    }
    override fun onStop() {
        Log.i(TAG, "onStop invoked...")
        super.onStop()
    }
    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView invoked...")
        super.onDestroyView()
    }
    override fun onDetach() {
        Log.i(TAG, "onDetach invoked...")
        super.onDetach()
    }
    ///////////////////////////////////////////////////////////////////////////
}
