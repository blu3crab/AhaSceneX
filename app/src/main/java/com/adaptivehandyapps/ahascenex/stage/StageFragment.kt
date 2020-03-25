//
// Created by MAT on 28FEB2020.
//
package com.adaptivehandyapps.ahascenex.stage

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
//import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentStageBinding
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.adaptivehandyapps.ahascenex.model.StageType

/**
 * [Fragment] subclass as the default destination in the navigation.
 */
class StageFragment : Fragment() {
    private val TAG = "StageFragment"

    // for Room & permissions
    private lateinit var application : Application

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
            inflater, R.layout.fragment_stage, container, false)

        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)

        // Room: application & database for viewmodel instantiation
        //val application = requireNotNull(this.activity).application
        application = requireNotNull(this.activity).application     // application used for permissions

        val dataSource = StageDatabase.getInstance(application).stageDatabaseDao

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = StageViewModelFactory(dataSource, application)
        stageViewModel = ViewModelProvider(this, viewModelFactory).get(StageViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = stageViewModel

        // Sets the adapter of the photosGrid RecyclerView
        binding.sceneListGrid.adapter = SceneGridAdapter(SceneGridAdapter.OnClickListener {
            //viewModel.displayPropertyDetails(it)
            Log.d(TAG, "SceneGridAdapter OnClickListener")
            val testInt = 256
            val testString = "nada"

            // set stagemodel from listener
            val stageModel = it
            //val stageModel = stageViewModel.stageList.value!!.get(0)
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
                    .actionStageFragmentToCraftFragment(testInt, testString, stageModel))
//                        stageModelId, stageModelLabel, stageModelType, stageModelSceneSrcUrl))
        })
        // retain instance
        //retainInstance = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            Log.d(TAG, "savedInstanceState not NULL...")
        }
        // GALLERY launches photo gallery
        view.findViewById<Button>(R.id.button_gallery).setOnClickListener {
            Log.d(TAG, "button_gallery setOnClickListener...")
//            // check for permissions
//            checkPermissionForImage()
            // launch Gallery intent to select photo
            pickImageFromGallery()
        }
        // CLEAR clears DB
        view.findViewById<Button>(R.id.button_clear).setOnClickListener {
            Log.d(TAG, "button_clear setOnClickListener...")
            stageViewModel.clearStageList()
        }
        // CRAFT launches CraftFragment with empty StageModel
        view.findViewById<Button>(R.id.button_craft).setOnClickListener {
            val testInt = 256
            val testString = "nada"
            val stageModel: StageModel = StageModel()
            // extract stagemodel element by element
            //val stageModel = stageViewModel.stageList.value!!.get(0)
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
                        .actionStageFragmentToCraftFragment(testInt, testString, stageModel))
//                            stageModelId, stageModelLabel, stageModelType, stageModelSceneSrcUrl))
//                        .actionStageFragmentToMakeFragment(testInt, testString))
        }

    }

    private fun pickImageFromGallery() {
        // ACTION_PICK launches into Google Photos requires READ/WRITE permissions or denied on orientation changes
        val intent = Intent(Intent.ACTION_PICK)
        // ACTION_OPEN_DOCUMENT retains permissions for later display as well as launching into "local" phone gallery
        //val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        //val intent = Intent(Intent.ACTION_GET_CONTENT)    // local
        //val intent = Intent(Intent.ACTION_CHOOSER)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
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

                    if (stageViewModel.addStageModel(stageModel)) {
                        //val position = stageViewModel.stageList.value?.size!!.minus(1)
                        binding.sceneListGrid.adapter?.notifyItemInserted(position)
                        Log.d(TAG, "\nscenex notifyItemInserted at position " + position)
                    }
                    else {
                        binding.sceneListGrid.adapter?.notifyItemChanged(0)
                        Log.d(TAG, "\nscenex notifyItemInserted at position 0")
                    }
                }
                if (resultUri == null) {
                    Log.d(TAG, "\nscenex imageUri NULL...")
                }
            }
        }
    }

}
