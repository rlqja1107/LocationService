package com.rlqja.toyou.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class CalendarListViewModel: ViewModel() {

    var mCalendarList=MutableLiveData<ArrayList<Any>>()

    var EMPTY="e"
    var mTitle=MutableLiveData<Long>()
    var mCurrentTime:Long=0
     var mCenterPosition:Int=0
    private fun setTitle(time:Long){
        mCurrentTime=time
     //   mTitle.value=DateFormat.getDate(time,DateFormat.CALENDAR_HEADER_FORMAT)
    }
    fun initCalendarList(count:Int){
        mCalendarList.value=ArrayList()
        setCalendarList(GregorianCalendar(),count)
    }
    private fun setCalendarList(cal:GregorianCalendar, moveCalendar:Int){
        val calendarList=ArrayList<Any>()
            try {
                val calendar = GregorianCalendar(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + moveCalendar,
                    1,
                    0,
                    0,
                    0
                )
                calendarList.add(calendar.timeInMillis)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                val max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
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

        mCalendarList.value=calendarList
    }

}