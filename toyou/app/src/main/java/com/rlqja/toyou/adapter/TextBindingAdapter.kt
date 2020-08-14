package com.rlqja.toyou.adapter

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.lang.Exception
import java.util.*

class TextBindingAdapter {
        companion object {

                @JvmStatic
                @BindingAdapter(*["setTextColor"])
                fun setTextColor(text:TextView,calendar:Calendar){
                    try {
                        if (calendar.get(Calendar.DAY_OF_WEEK) == 1 || calendar.get(Calendar.DAY_OF_WEEK) == 7) {
                            text.setTextColor(Color.RED)

                        }
                    }
                    catch(e:Exception){
                        e.printStackTrace()
                    }
                }

            @JvmStatic
            @BindingAdapter(*["setDayText"])
            fun setDayText(view: TextView, calendar: Calendar) {
                try {

                    var gregorian = GregorianCalendar(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        0,
                        0,
                        0
                    )
                    view.text = DateFormat.getDate(gregorian.timeInMillis, DateFormat.DAY_FORMAT)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            @JvmStatic
            @BindingAdapter(*["setCalendarHeaderText"])
            fun setCalendarHeaderText(view: TextView, data: Long) {
                try {
                        view.text = DateFormat.getDate(data, DateFormat.CALENDAR_HEADER_FORMAT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

}