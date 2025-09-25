package com.example.agriconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriconnect.adapter.ProductAdapter
import com.example.agriconnect.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerNewProducts: RecyclerView
    private lateinit var welcomeText: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val productList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerNewProducts = view.findViewById(R.id.recyclerNewProducts)
        welcomeText = view.findViewById(R.id.welcomeText)

        recyclerNewProducts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        adapter = ProductAdapter(productList) { product ->
            // Handle click if you want to open a detailed view
        }
        recyclerNewProducts.adapter = adapter

        fetchProducts()
        displayUserName()

        return view
    }

    private fun fetchProducts() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                productList.clear()
                for (doc in snapshot.documents) {
                    val product = doc.toObject(Product::class.java)
                    product?.let { productList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // handle errors
            }
    }

    private fun displayUserName() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val username = document?.getString("displayName") ?: "User"
                    welcomeText.text = "Welcome, $username"
                }
                .addOnFailureListener {
                    welcomeText.text = "Welcome, User"
                }
        } else {
            welcomeText.text = "Welcome, Guest"
        }
    }
}
