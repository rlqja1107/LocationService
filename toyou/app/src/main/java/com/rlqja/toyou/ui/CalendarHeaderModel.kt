package com.rlqja.toyou.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarHeaderModel: ViewModel() {
     var mHeaderDate=MutableLiveData<Long>()
    fun setHeaderDate(headerDate:Long){
        this.mHeaderDate.value=headerDate
    }
}