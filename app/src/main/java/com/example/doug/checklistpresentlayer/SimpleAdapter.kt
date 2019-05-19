package com.example.doug.checklistpresentlayer

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.widget.TextView
import com.example.doug.checklistpresentlayer.SimpleViewHolder
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


class SimpleAdapter(private var ctx: Context,
                    private var mainView: RecyclerView,
                    private var myDataset: List<SimpleViewModel>,
                    private val popupPresent: KMutableProperty0<Boolean>,
                    private val layoutInflater: LayoutInflater,
                    private val currentTask: KMutableProperty0<SimpleViewModel>,
                    private val currentChecklist: KMutableProperty0<Checklist>,
                    private val currentUser: User) : RecyclerView.Adapter<SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.bindData(myDataset[position])
        holder.simpleTextView.setOnClickListener {
            val popupFunctionWindow = PopupWindow(ctx)
            val taskFunctionLayoutView = layoutInflater.inflate(R.layout.task_functions_layout, null)

            taskFunctionLayoutView.FunctionCloseButton.setOnClickListener {
                popupFunctionWindow.dismiss()

                popupPresent.set(false)
            }

            taskFunctionLayoutView.FunctionSettingsButton.setOnClickListener {
                popupFunctionWindow.dismiss()

                popupPresent.set(false)

                createSettingsPopup()
            }

            //Sets the delete button to remove the task
            taskFunctionLayoutView.FunctionDeleteButton.setOnClickListener {
                var data: MutableList<SimpleViewModel> = myDataset as MutableList<SimpleViewModel>
                data.removeAt(position)
                setDataset(data)
                currentChecklist.get().deleteTask(position, currentUser)
                GlobalScope.launch {
                    deleteListDataFile()
                    createListFile(currentChecklist.get())
                }
//            for(i in recyclerView.childCount downTo 0 step 1)
//            {
//                val tempChild = recyclerView.getChildAt(i)
//                if(tempChild is TaskBox)
//                {
//                    if(tempChild == currentTask)
//                    {
//                        recyclerView.removeView(recyclerView.getChildAt(i))
//                        //remove the task from the list, and delete it from the database
//                        currentChecklist.deleteTask(i, currentUser)
//
//                        //update the local file
//                        GlobalScope.launch {
//                            deleteListDataFile()
//                            createListFile(currentChecklist)
//                        }
//                    }
//                }
//            }

                popupFunctionWindow.dismiss()

                popupPresent.set(false)
            }

            popupFunctionWindow.contentView = taskFunctionLayoutView

            popupFunctionWindow.setOnDismissListener {
                PopupWindow.OnDismissListener {
                    popupPresent.set(false)
                }
            }
            if(!popupPresent.get()) {

                popupPresent.set(true)

                popupFunctionWindow.isFocusable()

                popupFunctionWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                currentTask.set(myDataset[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_simple_itemview
    }

    fun setDataset(dataset: List<SimpleViewModel>) {
        myDataset = dataset
        notifyDataSetChanged()
    }

    fun datasetChange()
    {
        notifyDataSetChanged()
    }

    private fun createSettingsPopup() {
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

                val popupSettingsChangeNameWindow = PopupWindow(ctx)

                val taskSettingsChangeNameLayoutView =
                    layoutInflater.inflate(R.layout.task_settings_name_change_popup, null)

                popupSettingsChangeNameWindow.contentView = taskSettingsChangeNameLayoutView

                taskSettingsChangeNameLayoutView.ChangeNameButton.setOnClickListener {

                    val newName = taskSettingsChangeNameLayoutView.NewNameText.text.toString()

                    currentTask?.get().simpleText = newName

                    currentChecklist.get().changeTaskName(taskCount, currentUser, newName)

                    popupPresent.set(false)

                    popupSettingsChangeNameWindow.dismiss()
                }

                popupSettingsChangeNameWindow.setOnDismissListener {
                    popupPresent.set(false)
                }

                taskSettingsChangeNameLayoutView.ChangeNameCancelButton.setOnClickListener {
                    popupPresent.set(false)

                    popupSettingsChangeNameWindow.dismiss()
                }

                popupSettingsChangeNameWindow.isFocusable = true

                popupSettingsChangeNameWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)
            }

            taskSettingsLayoutView.CloseButton.setOnClickListener {
                popupSettingsWindow.dismiss()
                popupPresent.set(false)
            }

            popupSettingsWindow.setOnDismissListener {
                popupPresent.set(false)
            }

            taskSettingsLayoutView.taskNameView.text = currentTask?.get().simpleText

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