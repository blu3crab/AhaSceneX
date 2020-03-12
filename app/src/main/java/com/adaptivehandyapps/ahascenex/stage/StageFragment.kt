package com.adaptivehandyapps.ahascenex.stage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R

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
        val root = inflater.inflate(R.layout.fragment_stage, container, false)

        val viewModelFactory = StageViewModelFactory()
        stageViewModel = ViewModelProvider(this, viewModelFactory).get(StageViewModel::class.java)

        return root
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
