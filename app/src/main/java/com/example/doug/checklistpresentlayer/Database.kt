package com.example.doug.checklistpresentlayer

import okhttp3.*
import java.io.IOException
import java.util.*

class Database( var uName: String ) {

    var body: String? = ""

    fun LogIn(Password: String) : UserPage//used only to login
    {
        var user = UserPage(0,"","","", "")
        FetchJsonLogin(Password)
        while (body == ""){}
        if (body == "failed" || body == "{\"Message\":\"username or password is wrong dude\"}" || body == "{\"Message\":\"An error has occurred.\"}") {
            user = UserPage(0,"","","", body)
        }
        else {
            var seprate: List<String>? = body?.split("[", "]", "{", "}", "\"", ":", ",")?.filter { it.isNotBlank() }

            if (seprate != null) {
                var i = 0
                var UID = 0
                var UN = ""
                var FN = ""
                var LN = ""
                for (item in seprate) {
                    if (i == 0) {
                        if (item == "UserID") {
                            i = 1
                        }
                        if (item == "Username") {
                            i = 2
                        }
                        if (item == "FName") {
                            i = 3
                        }
                        if (item == "Lname") {
                            i = 4
                        }
                    } else {
                        if (i == 1) {
                            UID = item.toInt()
                            i = 0
                        } else if (i == 2) {
                            UN = item
                            i = 0
                        } else if (i == 3) {
                            FN = item
                            i = 0
                        } else if (i == 4) {
                            LN = item
                            i = 0
                            user = UserPage(UID, UN, FN, LN, "none")
                        }
                    }
                }
            }
        }
        return user
    }

    fun GetListofLists() : MutableList<ListofLists>//returns all lists user has access to
    {
        var LoL = mutableListOf<ListofLists>()
        if (body == "")
            FetchJsonUser()
        while (body == ""){}
        if (body == "failed" || body == "{\"Message\":\"An error has occurred.\"}") {
            LoL.add(ListofLists("", body))
        }
        else {
            var seprate: List<String>? = body?.split("[", "]", "{", "}", ":", ",")?.filter { it.isNotBlank() }

            if (seprate != null) {
                var i = 0
                var x = 0
                var name = ""
                for (item in seprate) {
                    if (i == 0) {
                        if (item == "\"Checklists\"" || item == "\"Changes\"") {
                            i = 1
                        }
                    }
                    else if (i == 1) {
                        if (item == "\"Users\"")
                        {
                            i = 2
                        }
                    }
                    else if (i == 2)
                    {
                        if (item == "\"Name\"" && x == 0)
                        {
                            x == 1
                        }
                        else if (x == 1)
                        {
                            name = item.drop(1)
                            name = name.dropLast(1)
                            LoL.add(ListofLists(name, "none"))
                            x = 0
                            i = 0
                        }
                    }
                }
            }
        }
        return LoL
    }
/*{"Changes":[],"Notes":[],"Notes1":[],"Tasks":[],"Users1":[],"Users":[],"Groups":[],"Checklists":[{"Changes":[],"Tasks":
[{"Changes":[],"User":null,"TaskID":1,"ChecklistID":1,"Name":"Feed the Hog","Description":null,"HasDeadline":false,"Deadline":null,"Completed":false,"DateCompleted":null,"CompletedBy":null},
{"Changes":[],"User":null,"TaskID":2,"ChecklistID":1,"Name":"Be the marble","Description":null,"HasDeadline":false,"Deadline":null,"Completed":false,"DateCompleted":null,"CompletedBy":null},
{"Changes":[],"User":null,"TaskID":3,"ChecklistID":1,"Name":"work on the database","Description":null,"HasDeadline":false,"Deadline":null,"Completed":false,"DateCompleted":null,"CompletedBy":null},
{"Changes":[],"User":null,"TaskID":4,"ChecklistID":1,"Name":"create dummy tasks","Description":null,"HasDeadline":false,"Deadline":null,"Completed":false,"DateCompleted":null,"CompletedBy":null},
{"Changes":[],"User":null,"TaskID":5,"ChecklistID":1,"Name":"test task","Description":null,"HasDeadline":false,"Deadline":null,"Completed":false,"DateCompleted":null,"CompletedBy":null}],
"Users":[],"ChecklistID":1,"Name":"Sample Checklist"}],"UserID":100000002,"Username":"u","FName":"cad","Lname":"mcnIv","pw":"p"}*/

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
                var name = ""
                var dis = ""
                var dl = ""
                var check = ""
                for (item in seprate) {
                    if (i == 0) {
                        if (item == "\"Checklists\"" || item == "\"Changes\"")
                        {//finds the start of a list, skipping user
                            i = 1
                        }
                        else if (item == "\"ChecklistID\"")
                        {//this is to find the id of the list we want. makes the next if go on the next loop
                            y = 1
                        }
                        else if (y == 1)
                        {//this checks that the tasks we read in are the right tasks. if yes exit loop if no emty list
                            check = item.drop(1)
                            check = check.dropLast(1)
                            if (check.toInt() == ID)
                            {
                                break
                            }
                            else
                            {
                                LoL = mutableListOf<Task>()
                                y = 0
                            }
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
                            if (item == "null") {
                                x = 4
                            } else if (item == "Deadline") {
                                x = 3
                            }
                        }
                        else if (x == 3)
                        {
                            dl = item.drop(1)
                            dl = dl.dropLast(1)
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
        return error
    }

    fun FetchJsonUser()//used whenever we need info for a user
    {
        val url = "https://api20190207120410.azurewebsites.net/Api/Users/GetString/" + uName
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

    fun FetchJsonLogin(password: String)//used for login only
    {
        val url = "https://api20190207120410.azurewebsites.net/Api/Users/login/" + uName + '/' + password
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