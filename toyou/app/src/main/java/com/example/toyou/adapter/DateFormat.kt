package com.example.toyou.adapter

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DateFormat {
    companion object{
        const val CALENDAR_HEADER_FORMAT:String="yyyy년 MM월"
        const val DAY_FORMAT="d"
        fun getDate(date:Long,pattern:String):String{
            try{
                var format=SimpleDateFormat(pattern, Locale.ENGLISH)
                var day=Date(date)
                return  format.format(day).toUpperCase()
            } catch(e:Exception){
                return ""
            }
        }
    }

}