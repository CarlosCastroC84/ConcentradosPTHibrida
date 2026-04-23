package com.example.concentradospt.ui.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class InventarioFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // TODO: Inflar el layout correspondiente: inventario_fragment.xml o similar
        return inflater.inflate(android.R.layout.simple_list_item_1, container, false)
    }
}
