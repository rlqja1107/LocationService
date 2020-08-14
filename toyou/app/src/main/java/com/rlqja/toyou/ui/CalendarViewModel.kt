package com.rlqja.toyou.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class CalendarViewModel:ViewModel() {

    var mCalendar=MutableLiveData<Calendar>()
    fun setCalendar(calendar: Calendar){
        this.mCalendar.value=calendar
    }


}