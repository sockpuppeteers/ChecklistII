package com.example.doug.checklistpresentlayer

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlin.concurrent.thread
import khttp.*
import org.json.JSONObject

data class Task (@SerializedName("Name") var name : String = "") {
    @SerializedName("TaskID") var TaskID: Int? = null
    @SerializedName("ChecklistID") var ChecklistID:  Int? = null
    @SerializedName("Deadline") var Deadline : String? = null
    @SerializedName("DateCompleted") var compdatetime : String? =  null
    @SerializedName("UserID") var isRecurring : Int? = 0
    var error : String? = ""

    constructor(_name: String, deadline: String) : this(_name){
        Deadline = deadline
    }

    constructor(_name: String, deadline: String, key : Int?) : this(_name){
        Deadline = deadline
        TaskID = key
    }

    constructor(_name: String, taskID: Int?, checklistID: Int?, deadline: String?, completedAt: String?, recurring: Int?) : this(_name){
        TaskID = taskID
        ChecklistID = checklistID
        Deadline = deadline
        compdatetime = completedAt
        isRecurring = recurring
    }
}