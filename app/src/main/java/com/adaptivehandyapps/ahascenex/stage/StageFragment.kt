///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 28FEB2020.
//
package com.adaptivehandyapps.ahascenex.stage

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
//import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentStageBinding
import com.adaptivehandyapps.ahascenex.model.PropDatabase
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.adaptivehandyapps.ahascenex.model.StageType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * [Fragment] subclass as the default destination in the navigation.
 */
class StageFragment : Fragment() {
    private val TAG = "StageFragment"

    // for Room & permissions
    private lateinit var application: Application

    var IMAGE_PICK_CODE = 1000

    private lateinit var stageViewModel: StageViewModel
    private lateinit var binding: FragmentStageBinding

    ///////////////////////////////////////////////////////////////////////////
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get reference to the binding object and inflate the fragment views.
//        val binding: FragmentStageBinding = DataBindingUtil.inflate(
//            inflater, R.layout.fragment_stage, container, false)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_stage, container, false
        )

        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)

        // Room: application & database for viewmodel instantiation
        //val application = requireNotNull(this.activity).application
        application =
            requireNotNull(this.activity).application     // application used for permissions

//        val dataSource = StageDatabase.getInstance(application).stageDatabaseDao
        val stageDataSource = StageDatabase.getInstance(application).stageDatabaseDao
        val propDataSource = PropDatabase.getInstance(application).propDatabaseDao

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = StageViewModelFactory(stageDataSource, propDataSource, application)
        stageViewModel = ViewModelProvider(this, viewModelFactory).get(StageViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = stageViewModel

        // Sets the adapter of the photosGrid RecyclerView
        binding.sceneListGrid.adapter = SceneGridAdapter(SceneGridAdapter.OnClickListener {
            //viewModel.displayPropertyDetails(it)
            Log.d(TAG, "SceneGridAdapter OnClickListener")
//            val testInt = 256
//            val testString = "nada"

            // set stagemodel from listener
            val stageModel = it
            // retain a active stage
            stageViewModel.setActiveStageListInx(stageModel)

            // TEST fragment IPC
            // extract stagemodel element by element
//            var stageModelId = "nada"
//            var stageModelLabel = "nada"
//            var stageModelType = "nada"
//            var stageModelSceneSrcUrl = "nada"
//            stageModel?.let {
//                stageModelId = stageModel.id
//                stageModelLabel = stageModel.label
//                stageModelType = stageModel.type
//                stageModelSceneSrcUrl = stageModel.sceneSrcUrl
//            }
            view!!.findNavController()
                .navigate(
                    StageFragmentDirections
                        .actionStageFragmentToCraftFragment(stageModel)
                )
//                .actionStageFragmentToCraftFragment(testInt, testString, stageModel))
//                        stageModelId, stageModelLabel, stageModelType, stageModelSceneSrcUrl))
        })
        // retain instance
        //retainInstance = true

        return binding.root
    }

    override fun onViewCreated(viewStage: View, savedInstanceState: Bundle?) {
        super.onViewCreated(viewStage, savedInstanceState)

        savedInstanceState?.let {
            Log.d(TAG, "savedInstanceState not NULL...")
        }
        // add FAB launches gallery selection
        val fabStageAdd = viewStage.findViewById<FloatingActionButton>(R.id.fab_stage_add)
        fabStageAdd.setOnClickListener { view ->
            Snackbar.make(view, "Add a stage...", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
            // launch Gallery intent to select photo
            pickImageFromGallery()
        }
        // remove FAB removes the recent selection from the stage grid
        val fabStageRemove = viewStage.findViewById<FloatingActionButton>(R.id.fab_stage_remove)
        fabStageRemove.setOnClickListener { view ->
            // get active stage
            var stageModel = stageViewModel.getActiveStage()
            // if active stage exists
            stageModel?.let {
                // remove active stage
                Snackbar.make(view, "Remove a stage...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                // TODO: remove current stage & associated props
                // remove from DB
                Log.d(TAG, "deletePropForStage...")
                stageViewModel.deletePropForStage(stageModel)
                Log.d(TAG, "deleteStageFromDatabase...")
                stageViewModel.deleteStageFromDatabase(stageModel)

                // remove from stage list
                if (stageViewModel.removeActiveStageFromList()) {
                    Log.d(TAG, "fabStageRemove notifyDataSetChanged...")
                    //binding.sceneListGrid.adapter?.notifyItemChanged(0)
                    binding.sceneListGrid.adapter?.notifyDataSetChanged()
                    //viewStage.invalidate()
                }
                else Log.d(TAG, "fabStageRemove removeActiveStageFromList FAIL...")

            } ?: run {
//            else {
                Snackbar.make(view, "StageGrid finds no stages to remove...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                // add alert dialog to confirm deleting all props
                val alertBuilder = AlertDialog.Builder(context)
                alertBuilder.setTitle("Confirm Remove Entire DB")
                alertBuilder.setMessage("Remove All Props and All Stages?")
                alertBuilder.setPositiveButton("YES") { dialog, which ->
                    // YES - remove all props
                    Toast.makeText(context, "YES - Removing all stages/props...", Toast.LENGTH_SHORT).show()
                    // clear prop DB
                    Log.d(TAG, "clearPropDatabase...")
                    stageViewModel.clearPropDatabase()
                    // clear stage DB
                    Log.d(TAG, "clearStageDatabase...")
                    stageViewModel.clearStageDatabase()
                }
                alertBuilder.setNegativeButton("Cancel") { dialog, which ->
                    // cancel - wrap
                    Toast.makeText(context, "CANCEL - NOT Removing all stages/props...", Toast.LENGTH_SHORT).show()
                }
                val alertDialog: AlertDialog = alertBuilder.create()
                alertDialog.show()
            }
        }

    }

    private fun pickImageFromGallery() {
        // TODO: add Prefs to select image source
        // ACTION_PICK launches into Google Photos requires READ/WRITE permissions or denied on orientation changes
//        val intent = Intent(Intent.ACTION_PICK)

        // ACTION_OPEN_DOCUMENT retains permissions for later display as well as launching into "local" phone gallery
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

        //val intent = Intent(Intent.ACTION_GET_CONTENT)    // local
        //val intent = Intent(Intent.ACTION_CHOOSER)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            IMAGE_PICK_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            if (data?.data != null) {
                val resultUri = data?.data
                // if imageUri is defined
                resultUri?.let {
                    Log.d(TAG, "\nscenex imageUri->" + resultUri)
                    // add to stage list
                    val position = stageViewModel.getStageListSize()
                    var stageModel = StageModel()
                    //stageModel.id = "1001"
                    stageModel.type = StageType.SCENE_TYPE.value
                    stageModel.sceneSrcUrl = resultUri.toString()
                    stageModel.label = "scene " + position

                    stageViewModel.addStageModel(stageModel)
                    binding.sceneListGrid.adapter?.notifyItemInserted(position)
                    Log.d(TAG, "\nscenex notifyItemInserted at position " + position)

//                    if (stageViewModel.addStageModel(stageModel)) {
//                        //val position = stageViewModel.stageList.value?.size!!.minus(1)
//                        binding.sceneListGrid.adapter?.notifyItemInserted(position)
//                        Log.d(TAG, "\nscenex notifyItemInserted at position " + position)
//                    } else {
//                        binding.sceneListGrid.adapter?.notifyItemChanged(0)
//                        Log.d(TAG, "\nscenex notifyItemInserted at position 0")
//                    }
                }
                if (resultUri == null) {
                    Log.d(TAG, "\nscenex imageUri NULL...")
                }
            }
        }
    }

}
