package com.example.doug.checklistpresentlayer

import okhttp3.*
import java.io.IOException
import java.util.*

class Database( var uName: String ) {

    var body: String? = ""

    fun LogIn(Password: String) : UserPage
    {
        var user = UserPage(0,"","","", "")
        FetchJson(Password)
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

    fun FetchJson(password: String)// : String?
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