package com.example.doug.checklistpresentlayer

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle

class SearchResultsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            //use the query to search your data somehow
            //TODO Cade, add database call here
            //query is the string the user entered into search user, you should be able to use that to search the database.
        }
    }
}