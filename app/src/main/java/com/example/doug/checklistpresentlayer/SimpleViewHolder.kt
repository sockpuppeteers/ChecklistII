package com.example.doug.checklistpresentlayer

import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox


class SimpleViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    val simpleTextView: TextView = itemView.findViewById(R.id.simple_text)
    private val simpleCheckView: CheckBox = itemView.findViewById(R.id.checkBox)

    fun bindData(viewModel: SimpleViewModel) {
        simpleTextView.text = viewModel.simpleText
        if(viewModel.isRecurring)
            simpleTextView.setTextColor(Color.RED)
        else
            simpleTextView.setTextColor(Color.BLACK)
        if(viewModel.isComplete)
            simpleTextView.apply { simpleTextView.paintFlags = simpleTextView.paintFlags!!.or(
                Paint.STRIKE_THRU_TEXT_FLAG) }
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}