///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 20MAR2020
//
package com.adaptivehandyapps.ahascenex.craft

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
//import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentCraftBinding
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CraftFragment : Fragment() {
    private val TAG = "CraftFragment"

    // for Room & permissions
    //private lateinit var application : Application

    private lateinit var craftViewModel: CraftViewModel
    private lateinit var binding: FragmentCraftBinding

//    private lateinit var stageModel: StageModel
//    private var stageModel: StageModel = StageModel()

    //private var view : View

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
        //application = requireNotNull(this.activity).application     // application used for permissions

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

//        stageModel.id = args.stageModelId
//        stageModel.label = args.stageModelLabel
//        stageModel.type = args.stageModelType
//        stageModel.sceneSrcUrl = args.stageModelSceneSrcUrl

        //Toast.makeText(context, "testInt: ${args.testInt}, testString: ${args.testString}", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "testInt: ${args.testInt}, testString: ${args.testString}", Toast.LENGTH_LONG).show()
        Log.d(TAG, "stageModel id# " + stageModel.nickname + " = " + stageModel.label + ", type " + stageModel.type + ", uri " + stageModel.sceneSrcUrl)

        return binding.root

    }

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

        // show scene
        craftViewModel.showScene(view)
//        val imgView = view.findViewById<ImageView>(R.id.imageview_scene)
//        val imgUrl = craftViewModel.stageModel.value!!.sceneSrcUrl
//        val imgUri = imgUrl!!.toUri()

//        craftViewModel.showScene(view, imgView, imgUri)
//        // show label
//        val editTextSceneLabel = view.findViewById<EditText>(R.id.edittext_scene_label)
//        editTextSceneLabel.setText(craftViewModel.stageModel.value!!.label)
    }
//    ///////////////////////////////////////////////////////////////////////////
//    fun showScene(view: View, imgView: ImageView, imgUri: Uri) {
//        // show label
//        val editTextSceneLabel = view.findViewById<EditText>(R.id.edittext_scene_label)
//        editTextSceneLabel.setText(craftViewModel.stageModel.value!!.label)
//        try {
//            Glide.with(imgView.context)
//                .load(imgUri)
//                .apply(
//                    RequestOptions()
//                        .placeholder(R.drawable.loading_animation)
//                        .error(R.drawable.ic_broken_image)
//                )
//                .into(imgView)
//        }
//        catch (ex : Exception) {
//            Log.e("BindingAdapter", "scenex Glide exception! " + ex.localizedMessage)
//        }
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
