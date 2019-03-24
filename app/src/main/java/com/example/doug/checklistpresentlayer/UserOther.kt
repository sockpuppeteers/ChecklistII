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
    private val h_User: User = User(mUID,mUserName,mFName,mLName,null,mError)

    fun ViewID() : Int?
    {
        return h_User.UserID
    }

    fun ViewUserName() : String
    {
        return h_User.Username
    }

    fun ViewFName() : String
    {
        return h_User.FName
    }

    fun ViewLName() : String
    {
        return h_User.LName
    }

    fun ViewError() : String?
    {
        return h_User.Error
    }

    fun ErrorCheck() : Boolean
    {
        var success = false
        if ( h_User.Error == "none")
        {
            success = true
        }
        return success
    }
}