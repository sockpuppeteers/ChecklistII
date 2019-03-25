package com.example.doug.checklistpresentlayer

data class Change(var checklistID: Int, var userID: Int, var taskID: Int, var taskName: String, var changedBy: String,
                  var changeType: kAction, var changedTo: String? = null)