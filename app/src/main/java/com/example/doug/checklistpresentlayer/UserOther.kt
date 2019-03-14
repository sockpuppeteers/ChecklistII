package com.example.doug.checklistpresentlayer

import android.app.Activity

data class User(var dUID: Int, var dUserName: String = "", var dFName: String = "",
                var dLName: String = "", val dError: String? = "" )

class UserPage(mUID: Int, mUserName: String, mFName: String, mLName: String, mError: String?)
{
    private val h_User: User = User(mUID,mUserName,mFName,mLName,mError)

    fun ViewID() : Int
    {
        return h_User.dUID
    }

    fun ViewUserName() : String
    {
        return h_User.dUserName
    }

    fun ViewFName() : String
    {
        return h_User.dFName
    }

    fun ViewLName() : String
    {
        return h_User.dLName
    }

    fun ViewError() : String?
    {
        return h_User.dError
    }

    fun ErrorCheck() : Boolean
    {
        var success = false
        if ( h_User.dError == "none")
        {
            success = true
        }
        return success
    }
}