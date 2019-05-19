package com.example.doug.checklistpresentlayer

import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox


class ChecklistViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    val ChecklistTextView: TextView = itemView.findViewById(R.id.checklist_text)
    private val ChecklistCheckView: CheckBox = itemView.findViewById(R.id.checkBox)

    fun bindData(viewModel: ChecklistViewModel) {
        ChecklistTextView.text = viewModel.ChecklistText
        if(viewModel.isRecurring)
            ChecklistTextView.setTextColor(Color.RED)
        else
            ChecklistTextView.setTextColor(Color.BLACK)
        if(viewModel.isComplete)
            ChecklistTextView.apply { ChecklistTextView.paintFlags = ChecklistTextView.paintFlags!!.or(
                Paint.STRIKE_THRU_TEXT_FLAG) }
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}