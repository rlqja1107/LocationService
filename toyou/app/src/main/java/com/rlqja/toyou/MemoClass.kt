package com.rlqja.toyou

import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

//memoOrCalendar가 true면 일정, false면 메모
open class PictureData(@PrimaryKey var id:String=UUID.randomUUID().toString(), var picture:ByteArray= byteArrayOf(), var distinguishId:String=""):RealmObject(){

}
open class MemoData(@PrimaryKey var id:String= UUID.randomUUID().toString(), var latitude:Double=0.0, var longitude:Double=0.0, var contents:String="", var title:String="",
                    var year:Int=0, var month:Int=0, var day:Int=0, var hour:Int=0, var minute:Int=0,var memoOrCalendar:Boolean=true,var picture: ByteArray?=null,var location:String?=null,var isDatePassed:Boolean=false):
    RealmObject(),Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readBoolean(),
        parcel.createByteArray(),
        parcel.readString(),
        parcel.readBoolean()
    ) {
    }

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
        this.location=data.location
        this.memoOrCalendar=data.memoOrCalendar
        this.picture=data.picture
        return this
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeDouble(latitude)
        dest?.writeDouble(longitude)
        dest?.writeString(contents)
        dest?.writeString(title)
        dest?.writeInt(year)
        dest?.writeInt(month)
        dest?.writeInt(day)
        dest?.writeInt(hour)
        dest?.writeInt(minute)
        dest?.writeBoolean(memoOrCalendar)
        dest?.writeByteArray(picture)
        dest?.writeString(location)
        dest?.writeBoolean(isDatePassed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MemoData> {
        override fun createFromParcel(parcel: Parcel): MemoData {
            return MemoData(parcel)
        }

        override fun newArray(size: Int): Array<MemoData?> {
            return arrayOfNulls(size)
        }
    }
}

class MemoClass(private val realm: Realm) {
    fun getPictureById(id:String):RealmResults<PictureData>{
        return realm.where(PictureData::class.java).equalTo("distinguishId",id)
            .findAll()
    }
    fun getAllMemo(): RealmResults<MemoData>{
        return realm.where(MemoData::class.java).equalTo("isDatePassed",false)
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