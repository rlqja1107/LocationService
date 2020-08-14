package com.rlqja.toyou

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rlqja.toyou.adapter.DailyListViewHolder
import io.realm.FieldAttribute
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.first_activity_main.*
import kotlinx.android.synthetic.main.main_dailylist_sample.view.*
import java.util.*

class FirstActivity:AppCompatActivity() {
    lateinit var realm: Realm
    lateinit var memo: RealmResults<MemoData>
    lateinit var now:GregorianCalendar
    lateinit var checkWord:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_activity_main)
        val checkingPermission=intent.getIntExtra("checkingPermission",0)
        val latitude=intent.getDoubleExtra("latitude", 37.56398)
        val longitude=intent.getDoubleExtra("longitude", 126.97935)
        Realm.init(this)
        mainMapImage.setOnClickListener {
            val mainIntent= Intent(this,MainActivity::class.java)
            mainIntent.putExtra("checkingPermission",checkingPermission)
            mainIntent.putExtra("latitude",latitude)
            mainIntent.putExtra("longitude",longitude)
            startActivity(mainIntent)
        }
        now=GregorianCalendar()
        todayList.text="오늘의 일정(${now.get(Calendar.YEAR)}.${now.get(Calendar.MONTH)+1}.${now.get(Calendar.DATE)})"
        val config = RealmConfiguration.Builder().migration { realm, oldVersion, newVersion ->
            val schema = realm.schema
            schema.create("MemoData").addField(
                "id",
                String::class.java,
                FieldAttribute.PRIMARY_KEY,
                FieldAttribute.REQUIRED
            )
                .addField("latitude", Double::class.java)
                .addField("longitude", Double::class.java)
                .addField("contents", String::class.java, FieldAttribute.REQUIRED)
                .addField("title", String::class.java, FieldAttribute.REQUIRED)
                .addField("year", Int::class.java)
                .addField("month", Int::class.java)
                .addField("day", Int::class.java)
                .addField("hour", Int::class.java)
                .addField("minute", Int::class.java)
                .addField("memoOrCalendar", Boolean::class.java)
        }.schemaVersion(1).build()
        Realm.setDefaultConfiguration(config)
        realm=Realm.getDefaultInstance()
        mainRecyclerView.layoutManager=LinearLayoutManager(this)
        mainCalendarText.setOnClickListener {
            val calendarIntent=Intent(this,CalendarActivity::class.java)
            startActivity(calendarIntent)
        }
        }

    override fun onResume() {
        memo=MemoClass(realm).getDayMemoData(now.get(Calendar.YEAR),now.get(Calendar.MONTH),
            now.get(Calendar.DATE))
        checkWord = if(memo.size==0) "오늘의 일정은 없습니다."
        else ""
        mainRecyclerView.adapter=DailyListAdapter()
        super.onResume()

    }

inner class DailyListAdapter:RecyclerView.Adapter<DailyListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
        return DailyListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_dailylist_sample,parent,false))
    }

    override fun getItemCount(): Int {
        return if(checkWord=="") memo.size
        else 1
    }

    override fun onBindViewHolder(holder: DailyListViewHolder, position: Int) {
        if(memo.size>0) {
            holder.itemView.mainTitle.text = "${position + 1}. ${memo[position]?.title}"
            holder.itemView.mainDay.text =
                "Time: ${memo[position]?.hour}시 ${memo[position]?.minute}"
        }
        else{
            holder.itemView.mainTitle.text=checkWord
            holder.itemView.mainDay.text="일정을 등록해주세요"
        }
        }

}
}