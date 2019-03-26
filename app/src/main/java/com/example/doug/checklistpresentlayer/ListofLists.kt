package com.example.doug.checklistpresentlayer
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import com.microsoft.windowsazure.mobileservices.*
import kotlinx.coroutines.runBlocking

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

    //This function will add a checklist to the database
    fun PostChecklist(checklist: ListClass){
        //create a json model of checklist
        val gson = Gson()
        val json = gson.toJson(checklist)

        runBlocking{
            //Make a post request
            val (request, response, result) = Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/")
                .header("Content-Type" to "application/json")
                .body(json.toString()).awaitStringResponseResult()

            //if the post is successful, we'll add the user to "lists" and connect the current user to the checklist
            //currently there is no error handling, but it will print the error message
            //we should probably do something in case of an error later
            result.fold(
                { data ->
                    var newList = gson.fromJson(data, ListClass::class.java)
                    lists.add(newList)

                    println("User ID: $uID")
                    println("newList.listID: ${newList.listID}")

                    AddUserToList(uID, newList.listID!!)
                    println("listID ${newList.listID}")
                },
                { error -> println("Error in PostChecklist function: ${error.message}") }
            )
        }
    }

    fun AddUserToList(userID: Int, checklistID: Int){
        //A Get request to this url will connect a user and checklist object in the database
        Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/${checklistID}/AddUser/${userID}")
            .response{request, response, result -> /*we're not doing anything with the result
                                                     but this function doesn't work without this line*/}
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