package com.example.doug.checklistpresentlayer

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlin.concurrent.thread
import khttp.*
import org.json.JSONObject

data class RecurringTask (@SerializedName("RecurringTaskID") var id : Int?, @SerializedName("TaskID") var TaskID: Int = -1,
                          @SerializedName("DateCompleted") var Day : String =  "",
                          @SerializedName("IsRecurring") var Time : String = "")