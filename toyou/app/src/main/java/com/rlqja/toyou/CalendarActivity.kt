package com.rlqja.toyou

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.rlqja.toyou.adapter.CalendarAdapter
import com.rlqja.toyou.adapter.DailyListViewHolder
import com.rlqja.toyou.databinding.CalendarListBinding
import com.rlqja.toyou.ui.CalendarListViewModel
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.calendar_activity.*
import kotlinx.android.synthetic.main.first_calendar_view.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class CalendarActivity:AppCompatActivity() {
    companion object{
        lateinit var memoData:RealmResults<MemoData>
        lateinit var context: Context
    }
    lateinit var binding: CalendarListBinding
    lateinit var calendarAdapter:CalendarAdapter
    lateinit var realm: Realm
    lateinit var calArray:ArrayList<Calendar>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)
        binding = DataBindingUtil.setContentView(this, R.layout.calendar_activity)
        context = this
        binding.setVariable(
            BR.model,
            ViewModelProvider.NewInstanceFactory().create(CalendarListViewModel::class.java)
        )
        binding.lifecycleOwner = this
        Realm.init(this)
        realm = Realm.getDefaultInstance()
        memoData = MemoClass(realm).getAllCalendarData()
        calArray = ArrayList<Calendar>().apply {
            for (i in memoData)
                this.add(GregorianCalendar(i.year, i.month, i.day))
        }
        calArray.sort()
        calendarPager.offscreenPageLimit=3
        calendarPager.currentItem=50
        val numArray=Array(100) { i->i-50}

        calendarPager.adapter=CalendarPagerAdapter(numArray)
        calendarPager.setPageTransformer(CalendarTransform())
        calendarPager.currentItem=50

    }
    private fun observe() {
        binding.model?.mCalendarList?.observe(this,
            Observer<ArrayList<Any>> {
                calendarAdapter.submitList(it)
            })
    }
    inner class CalendarTransform:ViewPager2.PageTransformer{
        private val pageMargin=resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
        private val pageOffset=resources.getDimensionPixelOffset(R.dimen.offset).toFloat()
        override fun transformPage(page: View, position: Float) {
            page.apply {
                val pageWidth = width
                val pageHeight = height

                val myOffset = position * -(2 * pageOffset + pageMargin)
                when {
                    position < -1 -> page.alpha = 0f
                    position <= 1 -> {
                        val scaleFactor = Math.max(0.85f, 1 - abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX=if(position<0) horzMargin-vertMargin/2
                        else horzMargin+vertMargin/2
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        alpha = (0.5f +
                                (((scaleFactor - 0.85f) / (1 - 0.85f)) * (1 - 0.5f)))
                    }
                    else -> {
                        page.alpha = 0f
                    }
                }
            }
        }

    }
    var compare=0
    inner class CalendarPagerAdapter(var numarray:Array<Int>):RecyclerView.Adapter<DailyListViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
            return DailyListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.first_calendar_view,parent,false))
        }

        override fun getItemCount(): Int=100

        override fun onBindViewHolder(holder: DailyListViewHolder, position: Int) {
            val calendar = GregorianCalendar(
                GregorianCalendar().get(Calendar.YEAR),
                GregorianCalendar().get(Calendar.MONTH) + numarray[position],
                1,
                0,
                0,
                0
            )
            calendarAdapter= CalendarAdapter(calArray) {

            }
            binding.model?.initCalendarList(numarray[position])
            holder.itemView.calendarRecyclerView.layoutManager=StaggeredGridLayoutManager(7,StaggeredGridLayoutManager.VERTICAL)
            holder.itemView.calendarRecyclerView.adapter=calendarAdapter
            observe()
        }


    }
}