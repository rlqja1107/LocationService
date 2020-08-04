package com.example.toyou.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.toyou.adapter.DateFormat
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class CalendarListViewModel: ViewModel() {

    var mCalendarList=MutableLiveData<ArrayList<Any>>()

    var EMPTY="e"
    var mTitle=MutableLiveData<String>()
    var mCurrentTime:Long=0
     var mCenterPosition:Int=0
    fun setTitle(position:Int){
        try{
            var item= mCalendarList.value?.get(position)
            if(item is Long)
                setTitle(item.toLong())
        }
        catch(e:Exception){
            e.printStackTrace()
        }
    }
    fun setTitle(time:Long){
        mCurrentTime=time
        mTitle.value=DateFormat.getDate(time,DateFormat.CALENDAR_HEADER_FORMAT)
    }
    fun initCalendarList(){
        println("First")
        setCalendarList(GregorianCalendar())
    }
    fun setCalendarList(cal:GregorianCalendar){
        setTitle(cal.timeInMillis)
        var calendarList=ArrayList<Any>()
        for(i in 0 until 36){
            try {
                var calendar = GregorianCalendar(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + i,
                    1,
                    0,
                    0,
                    0
                )
                calendarList.add(calendar.timeInMillis)
                var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                var max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (j in 0 until dayOfWeek) {
                    calendarList.add(EMPTY)
                }
                for (j in 1..max) {
                    calendarList.add(
                        GregorianCalendar(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            j
                        )
                    )
                }
            }
            catch(e:Exception){
                e.printStackTrace()
            }
        }
        mCalendarList.value=calendarList
        println("second")
    }

}