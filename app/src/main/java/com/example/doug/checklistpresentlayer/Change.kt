
package com.example.doug.checklistpresentlayer

data class Change(var taskName: String, var changedBy: User,
                  var changeType: kAction
) {
    var changedTo: String? = null
    constructor(_taskName: String, _changedBy: User, _changeType: kAction, _changedTo : String) : this(_taskName, _changedBy, _changeType){
        changedTo = _changedTo
    }
}