package com.adaptivehandyapps.ahascenex.stage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListAdapter
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentStageBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StageFragment : Fragment() {

    private val TAG = "StageFragment"

    private lateinit var stageViewModel: StageViewModel
    ///////////////////////////////////////////////////////////////////////////
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //val root = inflater.inflate(R.layout.fragment_stage, container, false)
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentStageBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_stage, container, false)

        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)

        // for Room
        val application = requireNotNull(this.activity).application

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = StageViewModelFactory()
        stageViewModel = ViewModelProvider(this, viewModelFactory).get(StageViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.stageViewModel = stageViewModel

        // bind the stage list adapter
//        val adapter = StageAdapter()
//        adapter = ListAdapter(this, android.R.layout.simple_list_item_1, stageViewModel.sceneNameList)
//        binding.stageList.adapter = adapter

//        // use arrayadapter and define an array
//        val arrayAdapter: ArrayAdapter<*>
//        val users = arrayOf(
//            "Virat Kohli", "Rohit Sharma", "Steve Smith",
//            "Kane Williamson", "Ross Taylor"
//        )
//
//        // access the listView from xml file
//        var mListView = binding.root.findViewById<ListView>(R.id.scene_list)
//        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
//        mListView.adapter = arrayAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_StageFragment_to_MakeFragment)
        }
//        https://codelabs.developers.google.com/codelabs/kotlin-android-training-start-external-activity/index.html?index=..%2F..android-kotlin-fundamentals#3
//        // Adding the parameters to the Action
//        view.findNavController()
//            .navigate(GameFragmentDirections
//                .actionGameFragmentToGameWonFragment(numQuestions, questionIndex))
//        val args = GameWonFragmentArgs.fromBundle(arguments!!)
//        Toast.makeText(context, "NumCorrect: ${args.numCorrect}, NumQuestions: ${args.numQuestions}", Toast.LENGTH_LONG).show()
    }
}
