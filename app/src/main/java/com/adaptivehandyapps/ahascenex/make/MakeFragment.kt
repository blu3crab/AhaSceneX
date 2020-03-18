package com.adaptivehandyapps.ahascenex.make

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.adaptivehandyapps.ahascenex.R
import com.adaptivehandyapps.ahascenex.databinding.FragmentMakeBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MakeFragment : Fragment() {

    private lateinit var makeViewModel: MakeViewModel
    private lateinit var binding: FragmentMakeBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_make, container, false)

        // Specify the current activity as the lifecycle owner of the binding.
        // This is necessary so that the binding can observe LiveData updates.
        binding.setLifecycleOwner(this)

        // reference to the ViewModel associated with this fragment
        val viewModelFactory = MakeViewModelFactory()
        makeViewModel = ViewModelProvider(this, viewModelFactory).get(MakeViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = makeViewModel

        val args = MakeFragmentArgs.fromBundle(arguments!!)
        Toast.makeText(context, "testInt: ${args.testInt}, testString: ${args.testString}", Toast.LENGTH_LONG).show()

//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_make, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_MakeFragment_to_StageFragment)
        }
    }
}
