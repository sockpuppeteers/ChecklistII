package com.example.doug.checklistpresentlayer

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.example.doug.checklistpresentlayer.SimpleViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup





class SimpleAdapter(private val myDataset: List<SimpleViewModel>) : RecyclerView.Adapter<SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.bindData(myDataset[position])
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_simple_itemview
    }
}