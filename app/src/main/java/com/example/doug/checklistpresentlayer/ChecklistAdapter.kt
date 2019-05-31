package com.example.doug.checklistpresentlayer

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.widget.TextView
import com.example.doug.checklistpresentlayer.ChecklistViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ScrollView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.task_functions_layout.view.*
import kotlinx.android.synthetic.main.task_settings_deadline_popup.view.*
import kotlinx.android.synthetic.main.task_settings_name_change_popup.view.*
import kotlinx.android.synthetic.main.task_settings_popup.view.*
import kotlinx.android.synthetic.main.task_settings_recursion_popup.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.KMutableProperty0


class ChecklistAdapter(private var ctx: Context,
                    private var mainView: RecyclerView,
                    private var myDataset: List<ChecklistViewModel>,
                    private val popupPresent: KMutableProperty0<Boolean>,
                    private val layoutInflater: LayoutInflater,
                    private val currentTask: KMutableProperty0<ChecklistViewModel>,
                    private val currentChecklist: KMutableProperty0<Checklist>,
                    private val currentUser: User) : RecyclerView.Adapter<ChecklistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ChecklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        holder.bindData(myDataset[position])
        holder.ChecklistCheckView.setOnClickListener {
            holder.vm.isChecked = !holder.vm.isChecked
        }
        holder.ChecklistTextView.setOnClickListener {
            if (!holder.vm.isMessage) {
                val popupFunctionWindow = PopupWindow(ctx)
                val taskFunctionLayoutView : View = if (!holder.vm.isComplete)
                    layoutInflater.inflate(R.layout.task_functions_layout, null)
                else
                    layoutInflater.inflate(R.layout.task_functions_nosettings_layout, null)

                taskFunctionLayoutView.FunctionCloseButton.setOnClickListener {
                    popupFunctionWindow.dismiss()

                    popupPresent.set(false)
                }

                if (!holder.vm.isComplete && holder.vm.isRecurring) {
                    taskFunctionLayoutView.FunctionSettingsButton.setOnClickListener {
                        popupFunctionWindow.dismiss()

                        popupPresent.set(false)

                        createSettingsPopup(holder)
                    }
                }

                //Sets the delete button to remove the task
                taskFunctionLayoutView.FunctionDeleteButton.setOnClickListener {
                    var data: MutableList<ChecklistViewModel> = myDataset as MutableList<ChecklistViewModel>
                    data.removeAt(position)
                    setDataset(data)
                    currentChecklist.get().deleteTask(position, currentUser)
                    GlobalScope.launch {
                        deleteListDataFile()
                        createListFile(currentChecklist.get())
                    }


                    popupFunctionWindow.dismiss()

                    popupPresent.set(false)
                }

                popupFunctionWindow.contentView = taskFunctionLayoutView

                popupFunctionWindow.setOnDismissListener {
                    PopupWindow.OnDismissListener {
                        popupPresent.set(false)
                    }
                }
                if (!popupPresent.get()) {

                    popupPresent.set(true)

                    popupFunctionWindow.isFocusable()

                    popupFunctionWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                    currentTask.set(myDataset[position])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_checklist_itemview
    }

    fun setDataset(dataset: List<ChecklistViewModel>) {
        myDataset = dataset
        notifyDataSetChanged()
    }

    fun datasetChange()
    {
        notifyDataSetChanged()
    }

    private fun createSettingsPopup(holder: ChecklistViewHolder) {
        if (!popupPresent.get()) {
            popupPresent.set(true)

            val popupSettingsWindow = PopupWindow(ctx)

            val taskSettingsLayoutView =
                layoutInflater.inflate(R.layout.task_settings_popup, null)

            var taskCount = 0
            var found = false
            //Checks all current gui elements to see if they are checked
            while (taskCount < currentChecklist.get().tasks.count() && !found) {
                if(currentChecklist.get().tasks[taskCount].TaskID == currentTask?.get().taskID)
                {
                    found = true

                }
                else
                {
                    taskCount++
                }
            }

            popupSettingsWindow.contentView = taskSettingsLayoutView

            /**************
             *   Deadline Button Displays Deadline popup
             ***************/
            taskSettingsLayoutView.DeadlineButton.setOnClickListener {

                popupSettingsWindow.dismiss()
                var curDeadline = LocalDate.now().toString()
                val popupSettingsDeadlineWindow = PopupWindow(ctx)

                val taskSettingsDeadlineLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_deadline_popup, null)

                var tempString = ""

                popupSettingsDeadlineWindow.contentView = taskSettingsDeadlineLayoutView

                if(currentChecklist.get().tasks[taskCount].Deadline != null) {
                    tempString =
                            ctx.getString(R.string.CURRENT_DEADLINE_TEXT) + " " + currentChecklist.get().tasks[taskCount].Deadline.toString()

                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = tempString
                }
                else {
                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = ctx.getString(R.string.NO_DEADLINE_TEXT)
                }

                taskSettingsDeadlineLayoutView.ClearDeadlineButton.setOnClickListener {
                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = ctx.getString(R.string.NO_DEADLINE_TEXT)

                    currentChecklist.get().removeDeadline(taskCount, currentUser)
                    curDeadline = LocalDate.now().minusDays(1).toString()
                }

                taskSettingsDeadlineLayoutView.DeadlineCalendarView.setOnDateChangeListener{_, year, month, day ->
                    tempString =
                            ctx.getString(R.string.CURRENT_DEADLINE_TEXT) + " " + year + "-" + month + "-" + day

                    taskSettingsDeadlineLayoutView.CurrentDeadlineTextView.text = tempString

                    curDeadline = "$year-$month-$day"
                }

                taskSettingsDeadlineLayoutView.closeDeadlineButton.setOnClickListener{

                    popupSettingsDeadlineWindow.dismiss()

                    popupPresent.set(false)
                }

                popupSettingsDeadlineWindow.setOnDismissListener {
                    if (curDeadline != LocalDate.now().minusDays(1).toString())
                        currentChecklist.get().changeTaskDeadline(taskCount, currentUser, curDeadline)
                    popupPresent.set(false)
                }

                popupSettingsDeadlineWindow.isFocusable = true

                popupSettingsDeadlineWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            /**************
             *   Recursion Button Displays Task Recursion popup
             ***************/
            taskSettingsLayoutView.RecursionButton.setOnClickListener {

                popupSettingsWindow.dismiss()

                val popupSettingsRecurringWindow = PopupWindow(ctx)

                val taskSettingsRecurringLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_recursion_popup, null)

                taskSettingsRecurringLayoutView.RecursionSwitch.setOnClickListener {
                    currentTask?.get().setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)
                }

                taskSettingsRecurringLayoutView.CurrentDaysTextView.text = calcTempStringDays(taskCount)

                taskSettingsRecurringLayoutView.CurrentTimeTextView.text = calcTempStringTime(taskCount)

                popupSettingsRecurringWindow.contentView = taskSettingsRecurringLayoutView

                popupSettingsRecurringWindow.setOnDismissListener {

                    currentTask?.get().setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)

                    popupPresent.set(false)

                }
                taskSettingsRecurringLayoutView.RecursionSwitch.setOnClickListener {
                    currentTask?.get().setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)
                    currentChecklist.get().setTaskRecursion(taskCount, currentUser,
                        taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)
                }

                taskSettingsRecurringLayoutView.CloseRecurringButton.setOnClickListener{

                    currentTask?.get().setRecurringIfNotComplete(taskSettingsRecurringLayoutView.RecursionSwitch.isChecked)

                    popupSettingsRecurringWindow.dismiss()

                    popupPresent.set(false)
                }

                //********************************************************************
                //Format for recurring date string is Day-Day-Day-.... ex. Mon-Tue-Fri
                //
                //Format for recurring time string is hour:minute AM\PM
                //********************************************************************
                taskSettingsRecurringLayoutView.SaveRecurringSettingsButton.setOnClickListener {
                    var dateString = ""

                    if(taskSettingsRecurringLayoutView.SundaySwitch.isChecked)
                        dateString += "Sun-"

                    if(taskSettingsRecurringLayoutView.MondaySwitch.isChecked)
                        dateString += "Mon-"

                    if(taskSettingsRecurringLayoutView.TuesdaySwitch.isChecked)
                        dateString += "Tue-"

                    if(taskSettingsRecurringLayoutView.WednesdaySwitch.isChecked)
                        dateString += "Wed-"

                    if(taskSettingsRecurringLayoutView.ThursdaySwitch.isChecked)
                        dateString += "Thu-"

                    if(taskSettingsRecurringLayoutView.FridaySwitch.isChecked)
                        dateString += "Fri-"

                    if(taskSettingsRecurringLayoutView.SaturdaySwitch.isChecked)
                        dateString += "Sat-"

                    if(dateString != currentChecklist.get().tasks[taskCount].recurringDays)
                        currentChecklist.get().updateTaskRecurringDays(taskCount, currentUser, dateString)

                    var timeString =
                        taskSettingsRecurringLayoutView.HourSpinner.selectedItem.toString() +
                                ":" + taskSettingsRecurringLayoutView.MinuteSpinner.selectedItem.toString() +
                                " " + taskSettingsRecurringLayoutView.AmPmSpinner.selectedItem.toString()

                    if(timeString != currentChecklist.get().tasks[taskCount].recurringTime)
                        currentChecklist.get().updateTaskRecurringTime(taskCount, currentUser, timeString)

                    taskSettingsRecurringLayoutView.CurrentDaysTextView.text = calcTempStringDays(taskCount)

                    taskSettingsRecurringLayoutView.CurrentTimeTextView.text = calcTempStringTime(taskCount)
                    holder.ChangeVisual()
                }

                taskSettingsRecurringLayoutView.RecursionSwitch.isChecked =
                        currentTask?.get().isRecurring != null && currentTask?.get().isRecurring == true


                popupSettingsRecurringWindow.setOnDismissListener {
                    popupPresent.set(false)
                }

                popupSettingsRecurringWindow.isFocusable = true

                popupSettingsRecurringWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            taskSettingsLayoutView.ChangeNameSettingsButton.setOnClickListener {

                popupSettingsWindow.dismiss()

                holder.editName(this::currentChecklist, taskCount, currentUser)
            }

            taskSettingsLayoutView.CloseButton.setOnClickListener {
                popupSettingsWindow.dismiss()
                popupPresent.set(false)
            }

            popupSettingsWindow.setOnDismissListener {
                popupPresent.set(false)
            }

            taskSettingsLayoutView.taskNameView.text = currentTask?.get().ChecklistText

            popupSettingsWindow.isFocusable = true

            popupSettingsWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
        }
    }

    private fun calcTempStringDays(index: Int) = when(currentChecklist.get().tasks[index].recurringDays != null) {
        true -> ctx.getString(R.string.CURRENT_RECURRING_DAYS_TEXT) +
                " " + currentChecklist.get().tasks[index].recurringDays
        false -> "No current recurring days"
    }

    private fun calcTempStringTime(index: Int) = when(currentChecklist.get().tasks[index].recurringTime != null){
        true -> ctx.getString(R.string.CURRENT_RECURRING_TIME_TEXT) +
                " " + currentChecklist.get().tasks[index].recurringTime
        false -> "No current recurring time"
    }

    fun deleteListDataFile(){
        //context will give us access to our local files directory
        val context = ctx.applicationContext

        val filename = currentChecklist.get().i_name
        val directory = context.filesDir

        //delete the file
        File(directory, filename).delete()
    }

    fun createListFile(list: Checklist) {
        //convert list to a JSON string
        val gson = Gson()
        val userJson = gson.toJson(list.tasks)

        //context will give us access to our local files directory
        var context = ctx.applicationContext

        val filename = list.i_name
        val directory = context.filesDir

        //write the file to local directory
        //the filename will be the name of the list
        val file = File(directory, filename)
        FileOutputStream(file).use {
            it.write(userJson.toByteArray())
        }
    }
}