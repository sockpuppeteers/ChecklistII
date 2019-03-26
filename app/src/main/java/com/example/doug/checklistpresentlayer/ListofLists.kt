package com.example.doug.checklistpresentlayer
import java.time.LocalDateTime
import com.microsoft.windowsazure.mobileservices.*

class ListofLists(var name: String, var error: String?, var uID : Int = 0) {

    //List of tasks within a checklist
    var lists =  mutableListOf<ListClass>()

    /****************************************************************
     *  Purpose: Creates an appropriate task with a name and
     *      description, shows who made it, and adds it to a list
     ***************************************************************/
    fun createList(name: String, createdBy: User) {
        val list = ListClass(null, name)
        lists.add(list)
    }

    /****************************************************************
     *  Purpose: Deletes a task from the list of tasks. Deletes
     *      through an index in the table and logs who deleted what
     ***************************************************************/
    fun deleteList(arrayIndex : Int, deletedBy: User) {
        if (arrayIndex >= 0 && arrayIndex < lists.size)
            lists.removeAt(arrayIndex)
    }

    /****************************************************************
     *  Purpose: Changes the name of a task to a user-specified new
     *      value. Shows which user modified it for logs.
     ***************************************************************/
    fun changeListName(arrayIndex: Int, modifiedBy: User, name: String) {
        if (arrayIndex >= 0 && arrayIndex < lists.size)
            lists[arrayIndex].i_name = name
    }

    fun setuId(id : Int) {
        uID = id
    }
}