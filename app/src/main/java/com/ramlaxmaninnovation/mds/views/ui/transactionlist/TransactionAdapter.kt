package com.ramlaxmaninnovation.mds.views.ui.transactionlist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ramlaxmaninnovation.mds.R
import kotlinx.android.synthetic.main.transaction_items.view.*

class TransactionAdapter  : RecyclerView.Adapter<TransactionAdapter.ProductListViewHolder>() {

    inner class ProductListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Data>() {

        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.last_consumption_date == newItem.last_consumption_date
            Log.i("TAG", "areItemsTheSame: "+oldItem.last_consumption_date)
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductListViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.transaction_items,
            parent,
            false
        )
    )
    override fun getItemCount() =  differ.currentList.size


    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val categoriesItem = differ.currentList[position]

        holder.itemView.apply {
            transaction_date_time.text = categoriesItem.last_consumption_date
            patient_id.text = categoriesItem.patient_id
            transaction_date_time.text = categoriesItem.last_consumption_date
            user_name.text = categoriesItem.last_consumption_date
            terminal_name.text = categoriesItem.device_name
            user_name.text=categoriesItem.nurse

        }
    }
}