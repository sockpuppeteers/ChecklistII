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
    @SerializedName("IsRecurring") var isRecurring : Boolean? = false
    @SerializedName("RecurringTime") var reccuringTime : String? = null
    @SerializedName("RecurringDays") var reccuringDays : String? = null

    constructor(_name: String, deadline: String) : this(_name){
        Deadline = deadline
    }

    constructor(_name: String, deadline: String?, key: Int?, checkID: Int?) : this(_name){
        Deadline = deadline
        TaskID = key
        ChecklistID = checkID
    }

    constructor(_name: String, taskID: Int?, checklistID: Int?, deadline: String?, completedAt: String?, recurring: Boolean?) : this(_name){
        TaskID = taskID
        ChecklistID = checklistID
        Deadline = deadline
        compdatetime = completedAt
        isRecurring = recurring
    }
}