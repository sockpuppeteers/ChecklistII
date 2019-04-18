package com.example.doug.checklistpresentlayer

import com.google.gson.annotations.SerializedName

data class Change(@SerializedName("ChecklistID") var checklistID: Int?, @SerializedName("UserID") var userID: Int,
                  @SerializedName("TaskID") var taskID: Int, var taskName: String, var changedBy: String,
                  @SerializedName("Action") var changeType: kAction, @SerializedName("Description") var changedTo: String? = null)