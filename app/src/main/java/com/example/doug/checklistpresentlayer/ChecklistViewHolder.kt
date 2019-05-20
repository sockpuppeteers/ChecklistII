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
    val ChecklistCheckView: CheckBox = itemView.findViewById(R.id.checkBox)
    lateinit var vm: ChecklistViewModel

    fun bindData(viewModel: ChecklistViewModel) {
        vm = viewModel
        ChecklistTextView.text = viewModel.ChecklistText
        if(viewModel.isRecurring)
            ChecklistTextView.setTextColor(Color.RED)
        else
            ChecklistTextView.setTextColor(Color.BLACK)
        if(viewModel.isComplete) {
            ChecklistTextView.apply {
                ChecklistTextView.paintFlags = ChecklistTextView.paintFlags!!.or(
                    Paint.STRIKE_THRU_TEXT_FLAG
                )
            }
            ChecklistCheckView.visibility = View.INVISIBLE
        }
        else {
            ChecklistTextView.apply {
                ChecklistTextView.paintFlags = 0
            }
            ChecklistCheckView.visibility = View.VISIBLE
        }
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}