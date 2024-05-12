package com.example.wondrobe.ui.add

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wondrobe.R
import com.example.wondrobe.databinding.FragmentAddBinding
import com.example.wondrobe.ui.add.post.SelectPost
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.closeAddFragment.setOnClickListener(closeFragmentOnClickListener)
        binding.postButton.setOnClickListener(postButtonOnClickListener)

        isCancelable = false

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private val closeFragmentOnClickListener = View.OnClickListener {
        dismiss()
    }

    private val postButtonOnClickListener = View.OnClickListener {
        try {
            val intent = Intent(requireContext(), SelectPost::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            //requireActivity().finish()
        } catch (e: Exception) {
            Log.e("AddFragment", "Error replacing fragment: $e")
        }
    }
}