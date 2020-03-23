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
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentStageBinding
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.adaptivehandyapps.ahascenex.model.StageType

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StageFragment : Fragment() {
    private val TAG = "StageFragment"

    // for Room
    private lateinit var application : Application

    var IMAGE_PICK_CODE = 1000
    var PERMISSION_CODE_READ = 1001
    var PERMISSION_CODE_WRITE = 1002


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
                    .actionStageFragmentToMakeFragment(testInt, testString, stageModel))
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
        view.findViewById<Button>(R.id.button_next).setOnClickListener {
            val testInt = 256
            val testString = "nada"
            // extract stagemodel element by element
            val stageModel = stageViewModel.stageList.value!!.get(0)
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
                        .actionStageFragmentToMakeFragment(testInt, testString, stageModel))
//                            stageModelId, stageModelLabel, stageModelType, stageModelSceneSrcUrl))
//                        .actionStageFragmentToMakeFragment(testInt, testString))
        }

        view.findViewById<Button>(R.id.button_gallery).setOnClickListener {
            Log.d(TAG, "button_gallery setOnClickListener...")
            // check for permissions
            checkPermissionForImage()
            // launch Gallery intent to select photo
            pickImageFromGallery()
        }
    }

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // if READ or WRITE permissions denied, request WRITE as it will bring along READ
            if ((checkSelfPermission(application, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (checkSelfPermission(application, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
                Log.d(TAG, "requesting permissions...")
                val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE_WRITE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult code " + requestCode)
        when (requestCode) {
            PERMISSION_CODE_WRITE -> if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Write Permission Granted!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, "Write Permission Denied!", Toast.LENGTH_SHORT)
                    .show()
                // kill app if denied
                Log.d(TAG, "onRequestPermissionsResult denied - finishAndRemoveTask...")
                //getActivity()?.finish(); // kills fragment not activity
                getActivity()?.finishAndRemoveTask()    // kills activity
            }
        }
    }

    private fun pickImageFromGallery() {
        // ACTION_OPEN_DOCUMENT retains permissions for later display as well as launching into "local" phone gallery
        //val intent = Intent(Intent.ACTION_PICK)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
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
