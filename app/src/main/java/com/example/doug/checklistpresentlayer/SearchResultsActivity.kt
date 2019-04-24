package com.example.doug.checklistpresentlayer

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.SparseArray



class SearchResultsActivity : Activity() {

    var users = SparseArray<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            val db = Database()

            println("in handleintent")
            users.append(0, db.GetUser(query))
            //db.GetUser(query)
        }
    }
}