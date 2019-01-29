package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import android.content.Intent
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_login.*


class MainActivity : AppCompatActivity() {

//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.navigation_home -> {
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_dashboard -> {
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_notifications -> {
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        navigation2.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        val user = UserPage(1000, "Scrumtaclause", "Nate", "Swenson", "123")
//
//        FLName.text = user.ViewFName() + " " + user.ViewLName()
//        UserName.text = "(" + user.ViewUserName() + ")"


        val body = FetchJson()
        val users = mutableListOf(UserPage(0,"","","",""))
        val seprate = body?.split("[","]","{","}","\"",":",",")?.filter { it.isNotBlank() }
        //println(seprate)
        if (seprate != null) {
            var i = 0
            var UID = 0
            var UN = ""
            var FN = ""
            var LN = ""
            for (item in seprate) {
                if (i == 0) {
                    if (item == "UserID") {
                        i = 1
                    }
                    if (item == "Username") {
                        i = 2
                    }
                    if (item == "FName") {
                        i = 3
                    }
                    if (item == "Lname") {
                        i = 4
                    }
                    if (item == "pw") {
                        i = 5
                    }
                }
                else
                {
                    if (i == 1)
                    {
                        UID = item.toInt()
                        i = 0
                    }
                    else if (i == 2)
                    {
                        UN = item
                        i = 0
                    }
                    else if (i == 3)
                    {
                        FN = item
                        i = 0
                    }
                    else if (i == 4)
                    {
                        LN = item
                        i = 0
                    }
                    else if (i == 5)
                    {
                        i = 0
                        val user = UserPage(UID,UN,FN,LN,item)
                        users.add(user)
                    }
                }
            }
        }
        var found = false
        login_button.setOnClickListener {
            for (item in users) {
                if (eUserName.text.toString() == item.ViewUserName() && item.CorectPW(ePW.text.toString())) {
                    val tempIntent = Intent(this, UserLogin::class.java).apply {
                        putExtra("uname", item.ViewUserName())
                        putExtra("fname", item.ViewFName())
                        putExtra("lname", item.ViewLName())
                    }
                    startActivity(tempIntent)
                    found = true
                    break
                }
            }
            if (!found) {
                ErrorText.text = "Wrong username or password"
            }
        }
    }

    fun FetchJson() : String?
    {
        val url = "https://api20190123020245.azurewebsites.net/api/users"
        var body: String? = ""
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }

            override fun onResponse(call: Call, response: Response) {
                body = response.body()?.string()
                println(body)

//                return if (body != null)
//                    Gson().fromJson(body, listOf<UserPage>().javaClass)
//                else
//                    listOf()

//                val gson = GsonBuilder().create()
//
//                val collectionType = object : TypeToken<List<UserPage>>(){}.type
//                val enums = gson.fromJson(body, collectionType::class.java)
//                val userpage = gson.fromJson(body, UserPage::class.java)
//                runOnUiThread{
//                    recyclerView_main.adapter = MainAdapter(userpage)
//                }
            }
        })
        Thread.sleep(2000)
        return body
    }
}

//class AllUsers(val users: List<UserPage>)
//{
//
//}
//
//class UserTemp(val UserID: Int, val Username: String, val Fname: String, val Lname: String, private val pw: String)
//{
//
//}