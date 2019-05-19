//package com.example.doug.checklistpresentlayer
//
//import android.os.Bundle
//import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.lang.Thread.sleep
//import java.util.*
//import kotlin.collections.ArrayList
//
//class TestActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_base_checklist)
//        var list: ArrayList<SimpleViewModel>  = generateSimpleList()
//        var adapter = SimpleAdapter(this, list)
//        val recyclerView = findViewById<RecyclerView>(R.id.simple_recyclerview)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.setHasFixedSize(true)
//        recyclerView.adapter = adapter
//        GlobalScope.launch {
//            sleep(5000)
//            list.add(SimpleViewModel(String.format(Locale.US, "NewItem")))
//            this@TestActivity.runOnUiThread {
////                recyclerView.adapter = adapter
//            }
//        }
//    }
//
//    private fun generateSimpleList(): ArrayList<SimpleViewModel> {
//        val simpleViewModelList = ArrayList<SimpleViewModel>()
//
//        for (i in 0..99) {
//            simpleViewModelList.add(SimpleViewModel(String.format(Locale.US, "This is item %d", i)))
//        }
//
//        return simpleViewModelList
//    }
//    private fun generateSimpleList2(): List<SimpleViewModel> {
//        val simpleViewModelList = ArrayList<SimpleViewModel>()
//
//        for (i in 99 downTo 0) {
//            simpleViewModelList.add(SimpleViewModel(String.format(Locale.US, "This is item %d", i)))
//        }
//
//        return simpleViewModelList
//    }
//}