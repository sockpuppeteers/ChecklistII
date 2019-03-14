package com.example.doug.checklistpresentlayer
import kotlin.concurrent.thread
import khttp.*

data class ListClass (var i_name: String, var error : String?) : ListTaskBase(i_name, "Checklist", true) {
    val p_key = Int
    fun PostObject(uID : Int)
    {
        //val payload = mapOf("Name" to i_name)
        thread() {
            val post1 = post("https://api20190207120410.azurewebsites.net/api/checklist/" + uID.toString(), data = mapOf("Name" to i_name))
            println(post1.statusCode)
        }
    }
}