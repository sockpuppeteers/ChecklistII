package com.example.doug.checklistpresentlayer
import android.provider.Settings
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.db.NULL
import org.joda.time.LocalDate


class Checklist( var name: String, var cListID : Int? ) : ListClass(cListID, name){
    private var dbAccess = Database()
    var tasks =  mutableListOf<Task>()
    var users = mutableListOf<User>()
    var changes = mutableListOf<Change>()



     /****************************************************************
     *  Purpose: Logs change, the user who changed it, the type of
     *      action that was taken, and what the value was changed to
     ***************************************************************/
    fun logChange(taskID: Int, taskName: String, changedBy: User, changeType: kAction, changedTo: String) {
        val change = Change(listID, changedBy.UserID!!, taskID, taskName, changedBy.Username, changeType, changedTo)
        changes.add(change)

        GlobalScope.launch{
            dbAccess.PostChange(change)
        }
    }

    /****************************************************************
     *  Purpose: Overloaded function of logChange that does not
     *      include a "changed to" value.
     ***************************************************************/
    fun logChange(taskID: Int, taskName: String, changedBy: User, changeType: kAction) {
        val change = Change(listID, changedBy.UserID!!, taskID, taskName, changedBy.Username, changeType, null)
        changes.add(change)

        GlobalScope.launch{
            dbAccess.PostChange(change)
        }
    }
    /****************************************************************
     *  Purpose: Overloaded log change function specifically for user
     *      changes to the checklist
     ***************************************************************/
    fun logChange(changeType: kAction, changedBy: User, changedTo: String)
    {
        val change = Change(listID, changedBy.UserID!!, -1, "", changedBy.Username, changeType, changedTo)
        changes.add(change)

        GlobalScope.launch{
            dbAccess.PostChange(change)
        }
    }

    fun setTaskRecursion(arrayIndex: Int, modifiedBy: User, toggle: Boolean?) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size && tasks[arrayIndex].compdatetime.isNullOrEmpty()) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, modifiedBy, kAction.CHANGE_TASK_RECURRING);
            tasks[arrayIndex].isRecurring = toggle
            dbAccess.PutTask(tasks[arrayIndex])
        }
    }


    fun updateTaskRecurringDays(arrayIndex: Int, modifiedBy: User, dateString: String){
        if (arrayIndex >= 0 && arrayIndex < tasks.size && tasks[arrayIndex].compdatetime.isNullOrEmpty()) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, modifiedBy, kAction.CHANGE_RECURRING_DAYS)
            tasks[arrayIndex].recurringDays = dateString

            dbAccess.PutTask(tasks[arrayIndex])
        }
    }

    fun updateTaskRecurringTime(arrayIndex: Int, modifiedBy: User, timeString: String){
        if (arrayIndex >= 0 && arrayIndex < tasks.size && tasks[arrayIndex].compdatetime.isNullOrEmpty()) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, modifiedBy, kAction.CHANGE_RECURRING_TIME)
            tasks[arrayIndex].recurringTime = timeString

            dbAccess.PutTask(tasks[arrayIndex])
        }
    }

    /****************************************************************
     *  Purpose: Overloaded function of create task that includes all
     *      previous information and also includes a deadline
     ***************************************************************/
    fun createTask(name: String, deadline: String?, createdBy: User, taskID: Int?, checklistID: Int?) {
        val task = Task(name, deadline, taskID, checklistID)
        task.TaskID = dbAccess.PostTask(task)
        tasks.add(task)
        logChange(task.TaskID!!, task.name, createdBy, kAction.CREATE_TASK)
    }

    /****************************************************************
     *  Purpose: Marks a task as completed and does NOT remove from
     *      the list of tasks. This is so it can be a recurring task
     ***************************************************************/
    fun completeTask(arrayIndex: Int, completedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, completedBy, kAction.COMPLETE_TASK)
            tasks[arrayIndex].compdatetime = LocalDate.now().toString()

            GlobalScope.launch {
                dbAccess.PutTask(tasks[arrayIndex])
            }
        }
    }

    fun uncompleteTask(arrayIndex: Int, completedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, completedBy, kAction.TASK_RECURRED)
            tasks[arrayIndex].compdatetime = null

            GlobalScope.launch {
                dbAccess.PutTask(tasks[arrayIndex])
            }
        }
    }

    /****************************************************************
     *  Purpose: Deletes a task from the list of tasks. Deletes
     *      through an index in the table and logs who deleted what
     ***************************************************************/
    fun deleteTask(arrayIndex : Int, deletedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, deletedBy, kAction.DELETE_TASK)
            dbAccess.DeleteTask(tasks[arrayIndex]) //remove the task from the database
            tasks.removeAt(arrayIndex) //remove the task from the app
        }
    }

    /****************************************************************
     *  Purpose: Changes the name of a task to a user-specified new
     *      value. Shows which user modified it for logs.
     ***************************************************************/
    fun changeTaskName(arrayIndex: Int, modifiedBy: User, name: String) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size && tasks[arrayIndex].compdatetime.isNullOrEmpty()) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, modifiedBy, kAction.CHANGE_TASK_NAME, name)
            tasks[arrayIndex].name = name
            GlobalScope.launch {
                dbAccess.PutTask(tasks[arrayIndex])
            }
        }
    }

    /****************************************************************
     *  Purpose: Changes the deadline of a task to a new value.
     *      Shows which user modified it for logs.
     ***************************************************************/
    fun changeTaskDeadline(arrayIndex: Int, modifiedBy: User, deadline: String) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size && tasks[arrayIndex].compdatetime.isNullOrEmpty()) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, modifiedBy, kAction.CHANGE_TASK_DEADLINE, deadline)
            tasks[arrayIndex].Deadline = deadline
            GlobalScope.launch {
                dbAccess.PutTask(tasks[arrayIndex])
            }
        }
    }

    /****************************************************************
     *  Purpose: Completely removes a deadline associated with a task
     *      as long as there is a deadline associated with it.
     ***************************************************************/
    fun removeDeadline(arrayIndex: Int, modifiedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size && tasks[arrayIndex].compdatetime.isNullOrEmpty()) {
            logChange(tasks[arrayIndex].TaskID!!, tasks[arrayIndex].name, modifiedBy, kAction.REMOVE_TASK_DEADLINE)
            tasks[arrayIndex].Deadline = null
            GlobalScope.launch {
                dbAccess.PutTask(tasks[arrayIndex])
            }
        }
    }

    fun addUser(currentUser: User, newUser: User){
        users.add(newUser)
        logChange(kAction.ADD_USER, currentUser, newUser.Username)
    }
}