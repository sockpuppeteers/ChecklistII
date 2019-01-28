package com.example.doug.checklistpresentlayer

data class Task (var name: String, var desc: String) {

    var HasDL : Boolean = false
    var Deadline : String = ""
    var completed : Boolean = false
    var compdatetime : String? =  null
    var compby : User? = null

    constructor(_name: String, _description: String, deadline: String) : this(_name, _description){
        Deadline = deadline
        HasDL = true
    }

}