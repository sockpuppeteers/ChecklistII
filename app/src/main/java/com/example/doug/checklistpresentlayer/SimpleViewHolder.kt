package com.example.doug.checklistpresentlayer

import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View


class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val simpleTextView: TextView = itemView.findViewById(R.id.simple_text)

    fun bindData(viewModel: SimpleViewModel) {
        simpleTextView.text = viewModel.simpleText
    }
}