package com.example.doug.checklistpresentlayer

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import android.view.KeyEvent.KEYCODE_ENTER
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0


class ChecklistViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    val ChecklistTextView: TextView = itemView.findViewById(R.id.checklist_text)
    val ChecklistEditView: EditText = itemView.findViewById(R.id.checklist_edit)
    val ChecklistCheckView: CheckBox = itemView.findViewById(R.id.checkBox)
    lateinit var vm: ChecklistViewModel

    fun bindData(viewModel: ChecklistViewModel) {
        vm = viewModel
        ChecklistTextView.text = viewModel.ChecklistText
        if(viewModel.isRecurring)
            ChecklistTextView.setTextColor(Color.parseColor("#99ccff"))
        else
            ChecklistTextView.setTextColor(Color.BLACK)
        when {
            viewModel.isComplete -> {
                ChecklistTextView.apply {
                    ChecklistTextView.paintFlags = ChecklistTextView.paintFlags!!.or(
                        Paint.STRIKE_THRU_TEXT_FLAG
                    )
                }
                ChecklistCheckView.visibility = View.INVISIBLE
            }
            viewModel.isMessage -> {
                ChecklistTextView.apply {
                    ChecklistTextView.paintFlags = 0
                }
                ChecklistCheckView.visibility = View.INVISIBLE
            }
            else -> {
                ChecklistTextView.apply {
                    ChecklistTextView.paintFlags = 0
                }
                ChecklistTextView.isClickable = false
                ChecklistCheckView.visibility = View.VISIBLE
            }
        }
    }

    fun editName(currentChecklist: KProperty0<KMutableProperty0<Checklist>>, taskCount: Int, currentUser: User)
    {
        ChecklistEditView.visibility = View.VISIBLE
        ChecklistEditView.hint = ChecklistTextView.text
        ChecklistTextView.visibility = View.INVISIBLE
        ChecklistEditView.requestFocus()
        ChecklistEditView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (ChecklistEditView.text.toString() != ChecklistTextView.text && !ChecklistEditView.text.isEmpty())
                {
                    currentChecklist.get().get().changeTaskName(taskCount, currentUser, ChecklistEditView.text.toString())
                }
                ChecklistTextView.text = ChecklistEditView.text.toString()
                vm.ChecklistText = ChecklistEditView.text.toString()
                ChecklistEditView.visibility = View.INVISIBLE
                ChecklistTextView.visibility = View.VISIBLE
                return@OnKeyListener true
            }
            else if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            {
                return@OnKeyListener true
            }
            else {
                ChecklistEditView.visibility = View.INVISIBLE
                ChecklistTextView.visibility = View.VISIBLE
                return@OnKeyListener false
            }
        })
    }

    fun ChangeVisual() {
        if (vm.isRecurring)
            ChecklistTextView.setTextColor(Color.parseColor("#99ccff"))
        else
            ChecklistTextView.setTextColor(Color.BLACK)
        when {
            vm.isComplete -> {
                ChecklistTextView.apply {
                    ChecklistTextView.paintFlags = ChecklistTextView.paintFlags!!.or(
                        Paint.STRIKE_THRU_TEXT_FLAG
                    )
                }
                ChecklistCheckView.visibility = View.INVISIBLE
            }
            vm.isMessage -> {
                ChecklistTextView.apply {
                    ChecklistTextView.paintFlags = 0
                }
                ChecklistCheckView.visibility = View.INVISIBLE
            }
            else -> {
                ChecklistTextView.apply {
                    ChecklistTextView.paintFlags = 0
                }
                ChecklistTextView.isClickable = false
                ChecklistCheckView.visibility = View.VISIBLE
            }
        }
    }
}