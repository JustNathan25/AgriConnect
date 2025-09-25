package com.example.agriconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agriconnect.R
import com.example.agriconnect.model.Product

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit // click listener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.productImage)
        val tvProductName: TextView = itemView.findViewById(R.id.productName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.productPrice)
        val tvProductLocation: TextView = itemView.findViewById(R.id.productLocation)
        val tvProductCondition: TextView = itemView.findViewById(R.id.productCondition)
        val tvProductDescription: TextView = itemView.findViewById(R.id.productDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = "₱%.2f".format(product.price)
        holder.tvProductLocation.text = product.locationName
        holder.tvProductCondition.text = "Condition: ${product.condition}"
        holder.tvProductDescription.text = product.description

        // Load the FIRST image (for preview in list)
        if (product.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.images[0]) // first image URL
                .placeholder(R.drawable.sample_product)
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.sample_product)
        }

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}
