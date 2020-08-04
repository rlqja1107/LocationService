package com.example.toyou

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.toyou.adapter.CalendarAdapter
import com.example.toyou.adapter.CalendarAdapter2
import com.example.toyou.databinding.CalendarListBinding
import com.example.toyou.ui.CalendarListViewModel
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList

class CalendarActivity:AppCompatActivity() {
    companion object{
        lateinit var memoData:RealmResults<MemoData>
        lateinit var context: Context
    }
    var EMPTY=""
    lateinit var binding: CalendarListBinding
    lateinit var calendarAdapter:CalendarAdapter
    var mCalendarList:MutableLiveData<ArrayList<Any>>?=null
    lateinit var realm: Realm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        context = this
        binding = DataBindingUtil.setContentView(this, R.layout.calendar_activity)
        Realm.init(this)
        realm = Realm.getDefaultInstance()
        memoData = MemoClass(realm).getAllMemo()
        val calArray = ArrayList<Calendar>().apply {
            for (i in memoData)
                this.add(GregorianCalendar(i.year, i.month, i.day))
        }
        calArray.sort()
        binding.setVariable(
            BR.model,
            ViewModelProvider.NewInstanceFactory().create(CalendarListViewModel::class.java)
        )
        binding.lifecycleOwner = this
        binding.model?.initCalendarList()
        calendarAdapter = CalendarAdapter(calArray) {
            var dialog = AlertDialog.Builder(this)
            dialog.setTitle("Toyou")
            dialog.show()
        }
        var manager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)

        binding.calendarRecyclerView.layoutManager = manager
        binding.calendarRecyclerView.adapter = calendarAdapter
        observe()
    }
    private fun observe(){
        binding.model?.mCalendarList?.observe(this,
            Observer<ArrayList<Any>> { t ->
                calendarAdapter.submitList(t)
                if(binding.model?.mCenterPosition!!>0){
                    binding.calendarRecyclerView.scrollToPosition(binding.model?.mCenterPosition!!)
                }
            })
    }
//    private fun setCalendarList(){
//        var cal= GregorianCalendar()
//        var calendarList=ArrayList<Any>()
//        for(i in -200 until 201){
//            try {
//                var calendar = GregorianCalendar(
//                    cal.get(Calendar.YEAR),
//                    cal.get(Calendar.MONTH) + i,
//                    1,
//                    0,
//                    0,
//                    0
//                )
//                calendarList.add(calendar.timeInMillis)
//                var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
//                var max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//                for (j in 0 until dayOfWeek) {
//                    calendarList.add(EMPTY)
//                }
//                for (j in 1..max) {
//                    calendarList.add(
//                        GregorianCalendar(
//                            calendar.get(Calendar.YEAR),
//                            calendar.get(Calendar.MONTH),
//                            j
//                        )
//                    )
//                }
//            }
//            catch(e:Exception){
//                e.printStackTrace()
//            }
//        }
//        mCalendarList?.value=calendarList
//    }
}