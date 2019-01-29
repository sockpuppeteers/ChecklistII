package com.example.doug.checklistpresentlayer

import android.app.Activity

data class Users(var dUID: Int, var dUserName: String = "", var dFName: String = "",
                var dLName: String = "", val dPw: String = "" )

class UserPage(mUID: Int, mUserName: String, mFName: String, mLName: String, private val mPw: String)
{
    private val h_User: Users = Users(mUID,mUserName,mFName,mLName,mPw)

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

    fun CorectPW(pw: String) : Boolean
    {
        var right = false
        if (pw == mPw)
        {
            right = true
        }
        return right
    }
}