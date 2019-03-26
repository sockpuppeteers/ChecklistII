package com.example.doug.checklistpresentlayer
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import com.microsoft.windowsazure.mobileservices.*

class ListofLists(var name: String, var error: String?, var uID : Int = 0) {

    //List of tasks within a checklist
    var lists =  mutableListOf<ListClass>()

    //Record of changes on a checklist
    var changes = mutableListOf<Change>()

    /****************************************************************
     *  Purpose: Creates an appropriate task with a name and
     *      description, shows who made it, and adds it to a list
     ***************************************************************/
    fun createList(name: String, createdBy: User) {
        val list = ListClass(null, name)
        PostChecklist(list)
    }

    fun PostChecklist(checklist: ListClass){
        //create a json model of checklist
        val gson = Gson()
        val json = gson.toJson(checklist)

        //post the object to the database
        Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/")
            .header("Content-Type" to "application/json")
            .body(json.toString())
            .response { req, res, result ->

                val gson = Gson()
                var newList = gson.fromJson(result.component1().toString(), ListClass::class.java)
                lists.add(newList)
                AddUserToList(uID, newList.listID!!)
            }
    }

    fun AddUserToList(userID: Int, checklistID: Int){
        Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/${checklistID}/AddUser/${userID}")
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