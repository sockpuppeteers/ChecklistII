package com.example.doug.checklistpresentlayer

data class Task (var i_name: String, var i_desc: String) : ListTaskBase(i_name, i_desc, false) {

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
}