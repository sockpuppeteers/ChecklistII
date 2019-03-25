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


class Database( var uName: String ) {
    var user = User(0, "", "", "", "","none")

    fun LogIn(Password: String) : UserPage {
        runBlocking {
            //make a login request to the API
            //the result is automatically deserialized into a User object with the gson library
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/Users/login/$uName/$Password").awaitObjectResponseResult(User.deserialize())

            //if the request is successful, copy the data into our user object
            //otherwise copy the error message
            result.fold(
                { data -> user = data
                  user.Error = "none"},
                { error -> user.Error = error.message }
            )
        }

        return UserPage(user.UserID, user.Username, user.FName, user.LName, user.Error)
    }

    //returns all lists user has access to
    fun GetListofLists(Username: String) : MutableList<ListClass> {
        var checklists = mutableListOf<ListClass>()

        runBlocking {
            //Do an api to get a list of all checklists the user is a part of
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/checklist/user/$uName").awaitStringResponseResult()

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

    fun RegisterUser(Email: String, FName: String, LName: String, PW1: String) : String {
        var error = ""
        runBlocking {
            //make a GET request to the api to see if a user with the given username already exists
            val (request, response, result) = Fuel.get("https://sockpuppeteerapi3.azurewebsites.net/Api/Users/GetString/$uName").awaitStringResponseResult()

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
            val newUser = User(null, uName, FName, LName, PW1, null)
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

    fun PostTask(task: Task){
        //create a json model of task
        val gson = Gson()
        val json = gson.toJson(task)

        //post the object to the database
        Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/task/")
            .header("Content-Type" to "application/json")
            .body(json.toString())
            .response { req, res, result -> /* you could do something with the response here */ }
    }

    fun PostChecklist(checklist: ListClass){
        //create a json model of checklist
        val gson = Gson()
        val json = gson.toJson(checklist)

        //post the object to the database
        Fuel.post("https://sockpuppeteerapi3.azurewebsites.net/api/checklist/")
            .header("Content-Type" to "application/json")
            .body(json.toString())
            .response { req, res, result -> /* you could do something with the response here */ }
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
}