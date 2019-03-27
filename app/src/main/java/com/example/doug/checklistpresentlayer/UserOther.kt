package com.example.doug.checklistpresentlayer

import android.app.Activity
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class User(@SerializedName("UserID") var UserID: Int?, @SerializedName("Username") var Username: String = "",
                @SerializedName("FName") var FName: String = "",
                @SerializedName("Lname") var LName: String = "", @SerializedName("pw") var Password: String? = "",
                var Error: String? = "" ){
    class deserialize: ResponseDeserializable<User>{
        override fun deserialize(content: String): User = Gson().fromJson(content, User::class.java)
    }
}

class UserPage(mUID: Int?, mUserName: String, mFName: String, mLName: String, mError: String?)
{
    private val h_User: User = User(mUID, mUserName, mFName, mLName, null, mError)

    /************************************
    * View functions are all just getters
    * ***********************************/

    fun ViewID() : Int? = h_User.UserID

    fun ViewUserName() : String = h_User.Username

    fun ViewFName() : String = h_User.FName

    fun ViewLName() : String = h_User.LName

    fun ViewError() : String? = h_User.Error

    //returns true if there's an error
    //or false if there's none
    fun HasError() : Boolean = h_User.Error != "none"
}