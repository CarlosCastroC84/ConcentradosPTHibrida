package com.example.concentradospt.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ReportesVentasFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // TODO: Inflar el layout correspondiente: reportes_fragment.xml o similar
        return inflater.inflate(android.R.layout.simple_list_item_1, container, false)
    }
}
