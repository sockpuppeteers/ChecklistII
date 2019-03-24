package com.example.doug.checklistpresentlayer
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlin.concurrent.thread
import khttp.*

data class ListClass (@SerializedName("ChecklistID") var listID: Int? = null,
                      @SerializedName("Name")var i_name: String, var error : String?) {

}