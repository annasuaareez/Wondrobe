package com.example.wondrobe.ui.add

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.wondrobe.databinding.FragmentAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddBinding? = null
    private var isOpened = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.closeAddFragment.setOnClickListener {
            dismiss() // Cierra el fragmento
        }

        isCancelable = false

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun show(manager: FragmentManager, tag: String?) {
        // Verificar si el fragmento ya está añadido
        /*val fragmentByTag = manager.findFragmentByTag(tag)
        if (fragmentByTag == null || !fragmentByTag.isAdded) {
            super.show(manager, tag)
        }*/
        if (!isOpened) {
            isOpened = true
            super.show(manager, tag)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isOpened = false // Marcar que el fragmento se ha cerrado
    }
}