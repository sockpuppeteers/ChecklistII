package com.example.doug.checklistpresentlayer

import android.content.Intent
import khttp.*
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_base_checklist.*
import kotlinx.android.synthetic.main.history_popup.view.*
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.task_functions_layout.view.*


/********************************************
 *TO DO: Move listener assignments to functions
 ********************************************/
class BaseListofLists : AppCompatActivity(){

    var inEdit = false

    var currentListofLists = ListofLists("Your CheckLists", "none")
    //Flag to see if any popups are present
    var popupPresent = false

    var currentTask: TaskBox? = null

    //Intialize things here
    init {


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_listoflists)
        currentListofLists.setuId(intent.getIntExtra("UserID", 0))
        var db = Database(intent.getStringExtra("uname"))
        currentListofLists.lists = db.GetListofLists()
        //Get layout of checklist names
        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)
        var tempBox: ListBox

        for (ListClass in currentListofLists.lists) {
            //Fill box with checklist name
            tempBox = ListBox(
                this,
                ListClass.i_name
            )

            //Set The action to be executed when the list in clicked
            tempBox.setOnClickListener{
                val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                    putExtra("ListName", ListClass.i_name)
                }

                //Start the BaseChecklist Activity
                startActivity(tempIntent)
            }

            //Add box to page
            taskLayout.addView(tempBox)
        }

        val addButton = findViewById<Button>(R.id.AddListButton)
        val editButton = findViewById<Button>(R.id.EditListButton)

        val addListener = View.OnClickListener {

            if(!popupPresent) {

                val mainView = findViewById<ScrollView>(R.id.TaskScrollView)

                val popupWindow = PopupWindow(this)

                val popupView = layoutInflater.inflate(R.layout.popup_layout, null)
                popupView.PopupEditText.hint = "Enter List Name"

                popupWindow.contentView = popupView

                val acceptButton = popupView.PopupMainView.AcceptButton

                //Creates and adds the on click action to the add button
                acceptButton.setOnClickListener{


                        val popup_edittext = popupView.PopupMainView.PopupEditText

                        val taskLayout = findViewById<LinearLayout>(R.id.TaskLayout)

                        if (popup_edittext.text.toString().length >= 1) {
                            var new_list_box = ListBox(
                                this,
                                popup_edittext.text.toString()
                            )

                            currentListofLists.createList(popup_edittext.text.toString(),
                                User(intent.getIntExtra("UserID", 0)))/*, intent.getStringExtra("uname"),
                                     intent.getStringExtra("fname"), intent.getStringExtra("lname")))*/

                            popupWindow.dismiss()

                            popupWindow.setOnDismissListener { PopupWindow.OnDismissListener {
                                popupPresent = false
                            } }

                            val popupFunctionWindow = PopupWindow(this)

                            val taskFunctionLayoutView =
                                layoutInflater.inflate(R.layout.task_functions_layout, null)

                            popupFunctionWindow.contentView = taskFunctionLayoutView

                            val DeleteButton = taskFunctionLayoutView.FunctionDeleteButton

                            val CloseButton = taskFunctionLayoutView.FunctionCloseButton

                            val CloseListener = View.OnClickListener {
                                popupFunctionWindow.dismiss()

                                popupPresent = false
                            }

                            val DeleteListener = View.OnClickListener {
                                for(i in TaskLayout.childCount downTo 0 step 1)
                                {
                                    val tempChild = TaskLayout.getChildAt(i)
                                    if(tempChild is TaskBox)
                                    {
                                        if(tempChild.getTaskText() == currentTask?.getTaskText())
                                        {
                                            TaskLayout.removeView(TaskLayout.getChildAt(i))
                                            currentListofLists.deleteList(i, User(1));
                                        }
                                    }
                                }

                                popupFunctionWindow.dismiss()

                                popupPresent = false
                            }

                            popupFunctionWindow.contentView = taskFunctionLayoutView

                            DeleteButton.setOnClickListener(DeleteListener)

                            CloseButton.setOnClickListener(CloseListener)

                            popupFunctionWindow.setOnDismissListener {
                                PopupWindow.OnDismissListener {
                                    popupPresent = false
                                }
                            }

                            new_list_box.setOnClickListener{
                                val tempIntent = Intent(this, BaseChecklist::class.java).apply {
                                    putExtra("ListName", popup_edittext.text.toString())
                                }
                                startActivity(tempIntent)
                            }

                            taskLayout.addView(new_list_box)
                        }
                }

                val cancelButton = popupView.PopupMainView.CancelButton

                cancelButton.setOnClickListener{

                    popupWindow.dismiss()

                }

                popupWindow.setOnDismissListener{
                    val popupEdittext = popupView.PopupMainView.PopupEditText

                    popupEdittext.text.clear()

                    popupPresent = false
                }

                popupWindow.isFocusable = true

                popupWindow.showAtLocation(mainView, Gravity.CENTER, 0, 0)

                popupPresent = true

            }
        }

        addButton.setOnClickListener(addListener)
    }
}
