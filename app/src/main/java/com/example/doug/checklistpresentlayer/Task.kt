package com.example.doug.checklistpresentlayer

import kotlin.concurrent.thread
import khttp.*
import org.json.JSONObject

data class Task (var i_name: String, var i_desc: String) {

    var HasDL : Boolean = false
    var Deadline : String = ""
    var completed : Boolean = false
    var compdatetime : String? =  null
    var compby : User? = null
    var error : String? = ""
    var db_key : Int = 0
    constructor(_name: String, _description: String, deadline: String) : this(_name, _description){
        Deadline = deadline
        HasDL = true
    }
    constructor(_name: String, _description: String, deadline: String, key : Int) : this(_name, _description){
        Deadline = deadline
        HasDL = true
        db_key = key
    }
    fun getId() : Int
    {
        return db_key
    }
    fun setId(key : Int)
    {
        db_key = key
    }
    fun postTask(key : Int)
    {
        thread()
        {
            val url = "https://api20190207120410.azurewebsites.net/api/task"
            val payload = "{\"ChecklistID\":12,\"Name\":\"" + i_name + "\"}"
            val r = post(url, data=payload, headers = mapOf("Content-Type" to "application/json"))

            println(r.statusCode)

        }
    }
    /*fun removeTask(key: Int)
    {
        thread()
        {
            val url = "https://api20190207120410.azurewebsites.net/api/task/"

            val r = delete(url + db_key.toString())

            println(r.statusCode)

        }
    }*/

    /*fun putTask(key : Int)
    {
        thread()
        {
            val url = "https://api20190207120410.azurewebsites.net/api/task"
            val payload = "{\"ChecklistID\":7,\"Name\":\"" + i_name + "\"}"
            val r = put(url, data = payload, headers = mapOf("Content-Type" to "application/json"))

            r.jsonObject
            r.statusCode
        }

    }*/
}