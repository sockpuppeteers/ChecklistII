package com.example.doug.checklistpresentlayer

import com.github.kittinunf.fuel.*
import kotlinx.coroutines.*
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.result.*
import okhttp3.*
import khttp.*
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread
import com.google.gson.Gson
import android.provider.MediaStore.Video
import com.google.gson.reflect.TypeToken


class Database {
    var user = User(0, "", "", "", "","none")

    fun LogIn(Username: String, Password: String) : UserPage {
        runBlocking {
            //make a login request to the API
            //the result is automatically deserialized into a User object with the gson library
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/Users/login/$Username/$Password").awaitObjectResponseResult(User.deserialize())

            //if the request is successful, copy the data into our user object
            //otherwise copy the error message
            result.fold(
                { data -> user = data
                  user.Error = "none"},
                { error -> user.Error = error.response.statusCode.toString()
                println(user.Error)}
            )
        }

        return UserPage(user.UserID, user.Username, user.FName, user.LName, user.Error)
    }

    //returns all lists user has access to
    fun GetListofLists(Username: String) : MutableList<ListClass> {
        var checklists = mutableListOf<ListClass>()

        runBlocking {
            //Do an api to get a list of all checklists the user is a part of
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/checklist/user/$Username").awaitStringResponseResult()

            //if the request is successful, copy the data into our checklists object
            //otherwise copy the error message
            result.fold(
                {
                    //create a json model of the return body
                    //the code is funky because the object is wrapped in a list, idk exactly how it works
                    val gson = Gson()
                    checklists = gson.fromJson(result.component1(), object : TypeToken<MutableList<ListClass>>() {}.type)},
                //print a message if the api call fails
                { println("api call failure in GetListofLists function") }
            )
        }

        return checklists
    }

    //returns all tasks within a list
    fun GetTasks(ID: Int) : MutableList<Task> {
        var tasks = mutableListOf<Task>()

        runBlocking {
            //Do an api to get a list of all tasks in a checklist
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/task/$ID").awaitStringResponseResult()

            //if the request is successful, copy the data into our tasks object
            //otherwise copy the error message
            result.fold(
                {
                    //create a json model of the return body
                    //the code is funky because the object is wrapped in a list, idk exactly how it works
                    val gson = Gson()
                    tasks = gson.fromJson(result.component1(), object : TypeToken<MutableList<Task>>() {}.type)},
                //print a message if the api call fails
                { println("api call failure in GetTasks function") }
            )
        }

        return tasks
    }

    fun RegisterUser(Username: String, Email: String, FName: String, LName: String, PW1: String) : String {
        var error = ""
        runBlocking {
            //make a GET request to the api to see if a user with the given username already exists
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/Users/GetString/$Username").awaitStringResponseResult()

            //if the username is taken, set an error message
            //otherwise do nothing
            result.fold(
                { error = "Username unavailable"}, {}
            )
        }

        //if there is no error then this gets called
        //and we can post the data to the database
        if (error == "") {
            //create a json model of a user object
            val newUser = User(null, Username, FName, LName, PW1, null)
            val gson = Gson()
            val json = gson.toJson(newUser)

            //post the object to the database
            Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/users/")
                .header("Content-Type" to "application/json")
                .body(json.toString())
                .response { req, res, result -> /* you could do something with the response here */ }
        }

        return error
    }

    fun PostTask(task: Task) : Int {
        //create a json model of task
        val gson = Gson()
        val json = gson.toJson(task)
        var listID = 0

        //this block will do a POST but it will also block the main thread while it waits for the
        //return data. The only reason we're doing this is because we NEED to get the taskID of the
        //newly created task. This is a temporary fix because we don't want to be blocking the main
        //thread if possible.
        runBlocking{
            //Make a post request
            val (request, response, result) = Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/task/")
                .header("Content-Type" to "application/json")
                .body(json.toString()).awaitStringResponseResult()

            //if the post is successful we'll get the taskID and return that
            result.fold(
                { data ->
                    var newTask = gson.fromJson(data, Task::class.java)
                    listID = newTask.TaskID!!
                },
                { error -> println("Error in PostChecklist function: ${error.message}") }
            )
        }

        return listID
    }

    fun PutTask(task: Task){
        //create a json model of task
        val gson = Gson()
        val json = gson.toJson(task)

        //make a put request
        Fuel.put("https://sockpuppeteerapi3.azurewebsites.net/api/task/${task.TaskID}")
            .header("Content-Type" to "application/json")
            .body(json.toString())
            .response { req, res, result -> /* you could do something with the response here */ }
    }

    fun DeleteTask(task: Task){
        //make a delete request
        Fuel.delete("https://sockpuppeteerapi3.azurewebsites.net/api/task/${task.TaskID}")
            .response { req, res, result -> /* you could do something with the response here */}
    }

    fun PutChecklist(checklist: ListClass){
        //create a json model of checklist
        val gson = Gson()
        val json = gson.toJson(checklist)

        //do a put request
        Fuel.put("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/${checklist.listID}")
            .header("Content-Type" to "application/json")
            .body(json.toString())
            .response { req, res, result -> /* you could do something with the response here */ }
    }

    fun AddUserToList(userID: Int, checklistID: Int){
        Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/${checklistID}/AddUser/${userID}")
    }

    //returns a list of users that have access to a given checklist
    fun GetUsers(checklistID: Int) : MutableList<User>{
        var users = mutableListOf<User>()

        runBlocking {
            //Do an api to get a list of all users in a checklist
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/checklist/$checklistID").awaitStringResponseResult()

            //if the request is successful, copy the data into our users object
            //otherwise copy the error message
            result.fold(
                {
                    //create a json model of the return body
                    //the code is funky because the object is wrapped in a list, idk exactly how it works
                    val gson = Gson()
                    users = gson.fromJson(result.component1(), object : TypeToken<MutableList<User>>() {}.type)},
                //print a message if the api call fails
                { println("api call failure in GetUsers function") }
            )
        }

        return users
    }

    fun PostChange(change: Change) {
        //create a json model of change
        val gson = Gson()
        val json = gson.toJson(change)

        runBlocking{
            //Make a post request
            val (request, response, result) = Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/change/")
                .header("Content-Type" to "application/json")
                .body(json.toString()).awaitStringResponseResult()

            //we don't do anything with the result
            result.fold(
                {/* if its successful */}, { /* if it fails */ }
            )
        }
    }

    //returns a list of changes within a checklist
    fun GetChanges(checklistID: Int) : MutableList<Change>{
        var changes = mutableListOf<Change>()

        runBlocking {
            //Do an api to get a list of all users in a checklist
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/change/$checklistID").awaitStringResponseResult()

            //if the request is successful, copy the data into our users object
            //otherwise copy the error message
            result.fold(
                {
                    //create a json model of the return body
                    //the code is funky because the object is wrapped in a list, idk exactly how it works
                    val gson = Gson()
                    changes = gson.fromJson(result.component1(), object : TypeToken<MutableList<Change>>() {}.type)},
                //print a message if the api call fails
                { println("api call failure in GetChanges function") }
            )
        }

        return changes
    }
}