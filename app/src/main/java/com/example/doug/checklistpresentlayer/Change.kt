package com.example.doug.checklistpresentlayer

import com.google.gson.annotations.SerializedName

data class Change(@SerializedName("ChecklistID") var checklistID: Int?, @SerializedName("UserID") var userID: Int,
                  @SerializedName("TaskID") var taskID: Int, @SerializedName("TaskName") var taskName: String,
                  @SerializedName("ChangedBy") var changedBy: String, @SerializedName("Action") var changeType: kAction,
                  @SerializedName("ChangedTo") var changedTo: String? = null)