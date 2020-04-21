///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020
//
package com.adaptivehandyapps.ahascenex.craft

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentCraftBinding
import com.adaptivehandyapps.ahascenex.formatStageModel
import com.adaptivehandyapps.ahascenex.model.PropDatabase
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar


//import kotlinx.android.synthetic.main.activity_main.*

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

        val stageDataSource = StageDatabase.getInstance(application).stageDatabaseDao
        val propDataSource = PropDatabase.getInstance(application).propDatabaseDao

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = CraftViewModelFactory(stageDataSource, propDataSource, context!!, application)
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

        Toast.makeText(context, "stageModel id#  ${stageModel.nickname} = ${stageModel.label}", Toast.LENGTH_SHORT).show()
        //Log.d(TAG, "stageModel id# " + stageModel.nickname + " = " + stageModel.label + ", type " + stageModel.type + ", uri " + stageModel.sceneSrcUrl)
        Log.d(TAG, "onCreateView SafeArgs-> " + formatStageModel(stageModel))

        return binding.root

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(viewCraft: View, savedInstanceState: Bundle?) {
        super.onViewCreated(viewCraft, savedInstanceState)
        // button_save_stage updates DB then navigates back to stage fragment
        viewCraft.findViewById<Button>(R.id.button_save_stage).setOnClickListener {
            // save stage model
            craftViewModel.saveStageModel(viewCraft)
            // navigate back to stage frag
            findNavController().navigate(R.id.action_CraftFragment_to_StageFragment)
        }
        // button_undo_stage restores stage model to previous state
        viewCraft.findViewById<Button>(R.id.button_undo_stage).setOnClickListener {
            // undo stage model
            craftViewModel.undoStageModel()
            craftViewModel.showScene(viewCraft)
        }
        // button_discard_stage removes the current stage model from the database
        viewCraft.findViewById<Button>(R.id.button_discard_stage).setOnClickListener {
            // discard props for stage model
            craftViewModel.deletePropModelDatabaseForStage()
            // discard stage model
            craftViewModel.deleteIdFromStageModelDatabase()
            // navigate back to stage frag
            findNavController().navigate(R.id.action_CraftFragment_to_StageFragment)
        }
        // imageview_scene touch interactions
        viewCraft.findViewById<ImageView>(R.id.imageview_scene).setOnTouchListener {
                motionView: View, motionEvent: MotionEvent ->
            craftViewModel.craftTouch.onTouch(motionView, motionEvent)
            // capture results of scene touch motion events
            craftViewModel.updateStageModelSceneTouch()
            true    // pass touch on
        }
        val fabCraftAdd = viewCraft.findViewById<FloatingActionButton>(R.id.fab_craft_add)
        fabCraftAdd.setOnClickListener { view ->
            Snackbar.make(view, "Craft adds a prop...", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
            craftViewModel.addPropView(viewCraft, null)
        }
        val fabCraftRemove = viewCraft.findViewById<FloatingActionButton>(R.id.fab_craft_remove)
        fabCraftRemove.setOnClickListener { view ->
            // if current prop is defined
            if (craftViewModel.currentPropIndex > -1) {
                // remove current prop
                Snackbar.make(view, "Craft removes a prop...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                craftViewModel.removeCurrentProp(viewCraft)
//                // TODO: redraw scene
////                viewCraft.invalidate()
//                // get prop list
//                craftViewModel.getPropList(viewCraft)
//                // show scene
//                craftViewModel.showScene(viewCraft)
//                viewCraft.invalidate()
            }
            else {
                Snackbar.make(view, "Craft finds no props to remove...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                // add alert dialog to confirm deleting all props
                val alertBuilder = AlertDialog.Builder(context)
                alertBuilder.setTitle("Confirm Remove All Props")
                alertBuilder.setMessage("Remove All Props in All Stages?")
                alertBuilder.setPositiveButton("YES") { dialog, which ->
                    // YES - remove all props
                    Toast.makeText(context, "YES - Removing all props...", Toast.LENGTH_SHORT).show()
                    craftViewModel.deletePropDatabase()
                }
                alertBuilder.setNegativeButton("Cancel") { dialog, which ->
                    // cancel - wrap
                    Toast.makeText(context, "CANCEL - NOT Removing all props...", Toast.LENGTH_SHORT).show()
                }
                val alertDialog: AlertDialog = alertBuilder.create()
                alertDialog.show()
            }
        }
        // get prop list
        craftViewModel.getPropList(viewCraft)

//        // show scene
//        craftViewModel.showScene(viewCraft)
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
