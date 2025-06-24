package com.example.onlineshopapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineshopapp.R
import com.example.onlineshopapp.data.model.CartItem
import com.example.onlineshopapp.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val onItemClick: (CartItem) -> Unit,
    private val onIncreaseClick: (CartItem) -> Unit,
    private val onDecreaseClick: (CartItem) -> Unit,
    private val onRemoveClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        init {            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
              binding.btnQuantityIncrease.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onIncreaseClick(getItem(position))
                }
            }
              binding.btnQuantityDecrease.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDecreaseClick(getItem(position))
                }
            }
              binding.btnRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemoveClick(getItem(position))
                }
            }
        }
        
        fun bind(cartItem: CartItem) {
            binding.apply {
                tvProductName.text = cartItem.productName
                
                val format = NumberFormat.getCurrencyInstance()
                format.currency = Currency.getInstance("USD")
                tvProductPrice.text = format.format(cartItem.productPrice)
                
                tvQuantity.text = cartItem.quantity.toString()
                
                // Calculate and format subtotal
                val subtotal = cartItem.productPrice * cartItem.quantity
                tvSubtotal.text = format.format(subtotal)                // Load image with Glide
                Glide.with(binding.root)
                    .load(cartItem.productImage)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .centerCrop()
                    .into(ivProduct)
            }
        }
    }
    
    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId && 
                   oldItem.quantity == newItem.quantity &&
                   oldItem.productPrice == newItem.productPrice
        }
    }
}
