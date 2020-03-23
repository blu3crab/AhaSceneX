package com.adaptivehandyapps.ahascenex.craft

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
//import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentCraftBinding
import com.adaptivehandyapps.ahascenex.model.StageDatabase
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CraftFragment : Fragment() {
    private val TAG = "CraftFragment"

    // for Room & permissions
    private lateinit var application : Application

    private lateinit var craftViewModel: CraftViewModel
    private lateinit var binding: FragmentCraftBinding

//    private lateinit var stageModel: StageModel
    private var stageModel: StageModel = StageModel()

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
        //val application = requireNotNull(this.activity).application
        application = requireNotNull(this.activity).application     // application used for permissions

        val dataSource = StageDatabase.getInstance(application).stageDatabaseDao

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = CraftViewModelFactory()
        craftViewModel = ViewModelProvider(this, viewModelFactory).get(CraftViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = craftViewModel

        val args = CraftFragmentArgs.fromBundle(arguments!!)

        stageModel = args.stageModel

        craftViewModel.setStageModel(stageModel)
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

        view.findViewById<Button>(R.id.button_stage).setOnClickListener {
            findNavController().navigate(R.id.action_CraftFragment_to_StageFragment)
        }

        // show scene
        val imgView = view.findViewById<ImageView>(R.id.imageview_scene)
        val imgUrl = stageModel.sceneSrcUrl
        val imgUri = imgUrl.toUri()

        showScene(imgView, imgUri)
    }

    fun showScene(imgView: ImageView, imgUri: Uri) {
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
}
