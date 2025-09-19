package com.example.agriconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriconnect.adapter.ProductAdapter
import com.example.agriconnect.model.Product

class HomeFragment : Fragment() {

    private lateinit var recyclerNewProducts: RecyclerView
    private lateinit var recyclerServices: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerNewProducts = view.findViewById(R.id.recyclerNewProducts)
        recyclerServices = view.findViewById(R.id.recyclerServices)

        recyclerNewProducts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerServices.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val sampleList = listOf(
            Product("Tomatoes", "₱100", "Cebu", R.drawable.sample_product),
            Product("Bananas", "₱60", "Davao", R.drawable.sample_product),
            Product("Carrots", "₱80", "Baguio", R.drawable.sample_product)
        )

        recyclerNewProducts.adapter = ProductAdapter(sampleList)
        recyclerServices.adapter = ProductAdapter(sampleList)

        return view
    }
}
