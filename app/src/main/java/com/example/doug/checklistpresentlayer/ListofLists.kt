package com.example.doug.checklistpresentlayer
import java.time.LocalDateTime


class ListofLists(var name: String ) {

    //List of tasks within a checklist
    var lists =  mutableListOf<ListClass>()

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
    fun createList(name: String, description: String, createdBy: User) {
        val list = ListClass(name, description)
        lists.add(list)
        logChange(name, createdBy, kAction.CREATE_TASK, name)
    }
    /****************************************************************
     *  Purpose: Deletes a task from the list of tasks. Deletes
     *      through an index in the table and logs who deleted what
     ***************************************************************/
    fun deleteList(arrayIndex : Int, deletedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < lists.size) {
            logChange(lists[arrayIndex].name, deletedBy, kAction.DELETE_TASK)
            lists.removeAt(arrayIndex)
        }
    }
    /****************************************************************
     *  Purpose: Changes the name of a task to a user-specified new
     *      value. Shows which user modified it for logs.
     ***************************************************************/
    fun changeListName(arrayIndex: Int, modifiedBy: User, name: String) {

        if (arrayIndex >= 0 && arrayIndex < lists.size) {
            logChange(lists[arrayIndex].name, modifiedBy, kAction.DELETE_TASK)
            lists[arrayIndex].name = name
        }
    }
    /****************************************************************
     *  Purpose: Changes the description of a task to a new value.
     *      Shows which user modified it for logs.
     ***************************************************************/
    fun changeListDescription(arrayIndex: Int, modifiedBy: User, description: String) {
        if (arrayIndex >= 0 && arrayIndex < lists.size) {
            logChange(lists[arrayIndex].name, modifiedBy, kAction.DELETE_TASK)
            lists[arrayIndex].desc = description
        }
    }
}