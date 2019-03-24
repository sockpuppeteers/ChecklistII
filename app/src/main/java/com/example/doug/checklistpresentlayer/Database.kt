package com.example.doug.checklistpresentlayer

import com.github.kittinunf.fuel.*
import kotlinx.coroutines.*
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.result.*
import okhttp3.*
import khttp.*
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class Database( var uName: String ) {

    var body: String? = ""

    fun LogIn(Password: String) : UserPage
    {
        //create a user object
        var user = User(0, "", "", "", "none")

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

    fun GetListofLists() : MutableList<ListClass>//returns all lists user has access to
    {
        var LoL = mutableListOf<ListClass>()
        if (body == "")
            FetchJsonUser()
        while (body == ""){}
        if (body == "failed" || body == "{\"Message\":\"An error has occurred.\"}") {
            LoL.add(ListClass("", body))
        }
        else {
            var seprate: List<String>? = body?.split("[", "]", "{", "}", ":", ",")?.filter { it.isNotBlank() }

            if (seprate != null) {
                var found_us = false
                var i = 0
                var x = 0
                var name = ""
                var id = 0
                for (item in seprate) {
                    if (i == 0) {
                        if (item == "\"Checklists\"" || item == "\"Changes\"") {
                            i = 1
                        }
                    }
                    else if (i == 1) {
                        if (item == "\"Users\"" && found_us)
                        {
                            i = 2
                        }
                        else if (item == "\"Users\"" && !found_us)
                        {
                            found_us = true
                        }
                    }
                    else if (i == 2)
                    {
                        if (item == "\"ChecklistID\"" && x == 0)
                        {
                            x = 1
                        }
                        else if (x == 1)
                        {
                            id = item.toInt()
                            x = 0
                        }
                        if (item == "\"Name\"" && x == 0)
                        {
                            x = 2
                        }
                        else if (x == 2)
                        {
                            name = item.drop(1)
                            name = name.dropLast(1)
                            val temp = ListClass(name, "none")
                            temp.p_key = id
                            LoL.add(temp)
                            x = 0
                            i = 0
                        }
                    }
                }
            }
        }
        return LoL
    }

    fun GetTasks(ID: Int) : MutableList<Task>//returns list of tasks for a list at ID on the database
    {
        var LoL = mutableListOf<Task>()
        if (body == "")
            FetchJsonUser()
        while (body == ""){}
        if (body == "failed" || body == "{\"Message\":\"An error has occurred.\"}") {
            val t = Task("","","")
            t.error = body
            LoL.add(t)
        }
        else {
            var seprate: List<String>? = body?.split("[", "]", "{", "}", ":", ",")?.filter { it.isNotBlank() }

            if (seprate != null) {
                var i = 0
                var x = 0
                var y = 0
                var z = 0
                var name = ""
                var dis = ""
                var dl = ""
                var currentlist = 0
                for (item in seprate) {
                    if (i == 0) {
                        if (item == "\"Checklists\"" || item == "\"Changes\"")
                        {//finds the start of a list, skipping user
                            i = 9
                        }
                    }
                    else if (i == 9) {
                        if (z == 1)
                        {
                            z = 0
                            if (item.toInt() == ID)
                            {
                                i = 1
                            }
                            else
                            {
                                i = 11
                                currentlist = item.toInt()
                            }
                        }
                        if (item == "\"ChecklistID\"") {
                            z = 1
                        }
                    }
                    else if (i == 11)
                    {
                        if (item == "\"Users\"")
                        {
                            i = 12
                        }
                    }
                    else if (i == 12)
                    {
                        if (z == 1)
                        {
                            z = 0
                            if (item.toInt() == currentlist)
                            {
                                i = 1
                            }
                            else
                            {
                                i = 9
                            }
                        }
                        if (item == "\"ChecklistID\"") {
                            z = 1
                        }
                    }
                    else if (i == 1) {
                        if (item == "\"Name\"")
                        {
                            i = 2
                        }
                        if (item == "\"Description\"")
                        {
                            i = 2
                            x = 1
                        }
                        if (item == "\"HasDeadline\"") {
                            i = 2
                            x = 2
                        }
                    }
                    else if (i == 2)
                    {
                        if (x == 0)
                        {
                            name = item.drop(1)//all items have "", these lines get rid of them
                            name = name.dropLast(1)
                            i = 1
                        }
                        else if (x == 1) {
                            if (item != "null") {
                                dis = item.drop(1)
                                dis = dis.dropLast(1)
                            } else {
                                dis = ""
                            }
                            i = 1
                        }
                        else if (x == 2) {
                            if (item == "false") {
                                x = 4
                            } else if (item == "\"Deadline\"") {
                                x = 3
                            }
                        }
                        else if (x == 3)
                        {
                            dl = item.drop(1)
                            dl = dl.dropLast(1)
                            x = 4
                        }
                        else if (x == 4) {
                            val t = Task(name, dis, dl)
                            t.error = "none"
                            LoL.add(t)
                            x = 0
                            i = 0
                        }
                    }
                }
            }
        }
        if (LoL.isEmpty())//checks if we found list. if not adds an error to list.
        {
            val t = Task("", "", "")
            t.error = "List is not in users lists"
            LoL.add(t)
        }
        return LoL
    }

    fun RegisterUser(Email: String, FName: String, LName: String, PW1: String, PW2: String) : String
    {
        var error: String = ""
        //only do anything if the passwords match
        if (PW1 == PW2) {
            FetchJsonUser()
            while (body == ""){}
            if (body != "{\"Message\":\"User with username = " + uName +" not found\"}")
                error = "Username Taken"
            else if (PW1.length > 16)
                error = "Password to long"
            else if (PW1.length < 4)
                error = "Password to short"
            else if (!Email.contains('@'))
            {
                error = "Must be a valid email"
            }
            else if (PW1.contains("([a-z]|[A-Z]|!|-|_|@|#|$|%|&|[0-9])*".toRegex())) {
                if (!(PW1.contains(".*([a-z]|[A-Z]).*".toRegex()))) {
                    error = "Password must contain a letter"
                }
                if (!(PW1.contains(".*[0-9].*".toRegex()))) {
                    if (error.isEmpty())
                        error = "Password must contain a number"
                    else
                        error += " and a number"
                }
                if (!(PW1.contains('!') || PW1.contains('-') || PW1.contains('_')
                            || PW1.contains('@') || PW1.contains('#') || PW1.contains('$')
                            || PW1.contains('%') || PW1.contains('&'))
                )
                {
                    if (error.isEmpty())
                        error = "Password must contain a nonstandard character"
                    else
                        error += " and a nonstandard character"
                }
            }
            else {
                error = "Password contains an unusable character"
            }
        }
        else
            error = "Passwords don't match"
        //if there is no error then this gets called
        //and we can post the data to the database
        if (error == "")
        {
            //payload is the body of the api request
            val payload = mapOf("Username" to uName, "FName" to FName, "LName" to LName, "pw" to PW1)
            //api calls can't be done in the main thread
            thread()
            {
                val url = "https://sockpuppeteerapi3.azurewebsites.net/api/users/"
                val r = post(url, data=JSONObject(payload), headers = mapOf("Content-Type" to "application/json"))
            }
        }
        return error
    }

    fun FetchJsonUser()//used whenever we need info for a user
    {
        val url = "https://sockpuppeteerapi3.azurewebsites.net/api/users/GetString/$uName"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
                body = "failed"
            }

            override fun onResponse(call: Call, response: Response) {
                body = response.body()?.string()
                println(body)
            }
        })
    }
}