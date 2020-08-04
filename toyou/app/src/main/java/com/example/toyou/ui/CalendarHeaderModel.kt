package com.example.toyou.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarHeaderModel: ViewModel() {
    public var mHeaderDate=MutableLiveData<Long>()
    fun setHeaderDate(headerDate:Long){
        this.mHeaderDate.value=headerDate
    }
}