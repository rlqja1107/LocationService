package com.rlqja.toyou

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import java.util.*

open class MemoData(@PrimaryKey var id:String= UUID.randomUUID().toString(), var latitude:Double=0.0, var longitude:Double=0.0, var contents:String="", var title:String="",
                    var year:Int=0, var month:Int=0, var day:Int=0, var hour:Int=0, var minute:Int=0,var memoOrCalendar:Boolean=true):
    RealmObject(){
    fun deepCopy(data:MemoData):MemoData{
        this.id=data.id
        this.latitude=data.latitude
        this.longitude=data.longitude
        this.contents=data.contents
        this.title=data.title
        this.year=data.year
        this.month=data.month
        this.day=data.day
        this.hour=data.hour
        this.minute=data.minute
        this.memoOrCalendar=data.memoOrCalendar
        return this
    }
}

class MemoClass(private val realm: Realm) {
    fun getAllMemo(): RealmResults<MemoData>{
        return realm.where(MemoData::class.java)
            .findAll()
    }
    fun getAllCalendarData():RealmResults<MemoData>{
        return realm.where(MemoData::class.java).equalTo("memoOrCalendar",true).findAll()
    }
    fun getDayMemoData(year:Int,month: Int,day: Int):RealmResults<MemoData>{
        return realm.where(MemoData::class.java).equalTo("year",year)
            .equalTo("month",month).equalTo("day",day)
            .equalTo("memoOrCalendar",true)
            .sort("hour")
            .findAll()
    }
}