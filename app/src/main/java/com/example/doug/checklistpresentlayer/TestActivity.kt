package com.example.doug.checklistpresentlayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import java.util.*

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_checklist)
        val adapter = SimpleAdapter(generateSimpleList())
        val recyclerView = findViewById<RecyclerView>(R.id.simple_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    private fun generateSimpleList(): List<SimpleViewModel> {
        val simpleViewModelList = ArrayList<SimpleViewModel>()

        for (i in 0..99) {
            simpleViewModelList.add(SimpleViewModel(String.format(Locale.US, "This is item %d", i)))
        }

        return simpleViewModelList
    }
}