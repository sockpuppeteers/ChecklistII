package com.example.doug.checklistpresentlayer
import java.time.LocalDateTime

class Checklist( var name: String ) {

    var uID = 0
    var checkID = 0
    //List of tasks within a checklist
    var tasks =  mutableListOf<Task>()

    //List of user included in a checklist
    var users = mutableListOf<User>()

    //Record of changes on a checklist
    var changes = mutableListOf<Change>()

     /****************************************************************
     *  Purpose: Logs change, the user who changed it, the type of
     *      action that was taken, and what the value was changed to
     ***************************************************************/
    fun logChange(taskChanged: String, changedBy: User, changeType: kAction, changedTo: String) {
        val change = Change(taskChanged, changedBy, changeType, changedTo)
        changes.add(change)
    }
    /****************************************************************
     *  Purpose: Overloaded function of logChange that does not
     *      include a "changed to" value.
     ***************************************************************/
    fun logChange(taskChanged: String, changedBy: User, changeType: kAction) {
        val change = Change(taskChanged, changedBy, changeType)
        changes.add(change)
    }
    /****************************************************************
     *  Purpose: Creates an appropriate task with a name and
     *      description, shows who made it, and adds it to a list
     ***************************************************************/
    fun createTask(name: String, createdBy: User) {
        val task = Task(name)
        tasks.add(task)
        logChange(name, createdBy, kAction.CREATE_TASK, name)
    }
    /****************************************************************
     *  Purpose: Overloaded function of create task that includes all
     *      previous information and also includes a deadline
     ***************************************************************/
    fun createTask(name: String, deadline: String, createdBy: User) {
        val task = Task(name, deadline)
        tasks.add(task)
        logChange(name, createdBy, kAction.CREATE_TASK, name)
    }
    /****************************************************************
     *  Purpose: Marks a task as completed and does NOT remove from
     *      the list of tasks. This is so it can be a recurring task
     ***************************************************************/
    fun completeTask(arrayIndex: Int, completedBy: User) {

        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].name, completedBy, kAction.COMPLETE_TASK)
            tasks[arrayIndex].compdatetime = LocalDateTime.now().toString()
        }
    }
    /****************************************************************
     *  Purpose: Overloaded completeTask function that allows a user
     *      to manually enter the time that a task was completed.
     ***************************************************************/
    fun completeTask(arrayIndex: Int, completedBy: User, datetimeWhenCompleted: String) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].name, completedBy, kAction.COMPLETE_TASK)
            tasks[arrayIndex].compdatetime = datetimeWhenCompleted
        }
    }
    /****************************************************************
     *  Purpose: Deletes a task from the list of tasks. Deletes
     *      through an index in the table and logs who deleted what
     ***************************************************************/
    fun deleteTask(arrayIndex : Int, deletedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].name, deletedBy, kAction.DELETE_TASK)
            //tasks[arrayIndex].removeTask(uID)
            tasks.removeAt(arrayIndex)

        }
    }
    /****************************************************************
     *  Purpose: Changes the name of a task to a user-specified new
     *      value. Shows which user modified it for logs.
     ***************************************************************/
    fun changeTaskName(arrayIndex: Int, modifiedBy: User, name: String) {

        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].name, modifiedBy, kAction.DELETE_TASK)
            //tasks[arrayIndex].putTask(uID)
            tasks[arrayIndex].name = name
        }
    }

    /****************************************************************
     *  Purpose: Changes the deadline of a task to a new value.
     *      Shows which user modified it for logs.
     ***************************************************************/
    fun changeTaskDeadline(arrayIndex: Int, modifiedBy: User, deadline: String) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].name, modifiedBy, kAction.DELETE_TASK)
            tasks[arrayIndex].Deadline = deadline
        }
    }
    /****************************************************************
     *  Purpose: Completely removes a deadline associated with a task
     *      as long as there is a deadline associated with it.
     ***************************************************************/
    fun removeDeadline(arrayIndex: Int, modifiedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < tasks.size) {
            logChange(tasks[arrayIndex].name, modifiedBy, kAction.DELETE_TASK)
            tasks[arrayIndex].Deadline = ""
        }
    }
}