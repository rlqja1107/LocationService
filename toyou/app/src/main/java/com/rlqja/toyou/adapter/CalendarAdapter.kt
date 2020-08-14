package com.rlqja.toyou.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import com.rlqja.toyou.CalendarActivity
import com.rlqja.toyou.MemoData
import com.rlqja.toyou.R
import com.rlqja.toyou.databinding.CalendarHeaderBinding
import com.rlqja.toyou.databinding.DayItemBinding
import com.rlqja.toyou.databinding.EmptyDayBinding
import com.rlqja.toyou.ui.CalendarHeaderModel
import com.rlqja.toyou.ui.CalendarViewModel
import com.rlqja.toyou.ui.EmptyViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_day.view.*
import java.util.*

class CalendarViewHolder  constructor( val binding: CalendarHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
     fun setViewModel(model: CalendarHeaderModel) {
        binding.model = model
        binding.executePendingBindings()
    }
}
 class EmptyViewHolder constructor(private val binding: EmptyDayBinding) :
    RecyclerView.ViewHolder(binding.root) {
     fun setViewModel(model: EmptyViewModel) {
        binding.model = model
        binding.executePendingBindings()
    }

}
internal class DayViewHolder constructor(private val binding: DayItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
     fun setViewModel(model: CalendarViewModel) {
        binding.model = model
        binding.executePendingBindings()
    }

}

class CalendarAdapter(var calArray:ArrayList<Calendar>,var click:(Calendar)->(Unit)):ListAdapter<Any, RecyclerView.ViewHolder>(object: DiffUtil.ItemCallback<Any>(){
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem==newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        var gson= Gson()
        return gson.toJson(oldItem) == gson.toJson(newItem)
    }
}) {

        val HEADER=1
        val EMPTY=2
        val DAY=3

     override fun getItemViewType(position: Int): Int {
        val items=getItem(position)
         if(items is Long)return HEADER
         else if(items is String)return EMPTY
         else return DAY
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType== HEADER){
            var binding=DataBindingUtil.inflate<CalendarHeaderBinding>(LayoutInflater.from(parent.context),R.layout.item_calendar_header,parent,false)
            var params=binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan=true
            binding.root.layoutParams=params
           return CalendarViewHolder(binding)
        }
        else if(viewType== EMPTY){
            var binding=DataBindingUtil.inflate<EmptyDayBinding>(LayoutInflater.from(parent.context),R.layout.item_day_empty,parent,false)
            return EmptyViewHolder(binding)
        }
        else {
            val binding = DataBindingUtil.inflate<DayItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_day,
                parent,
                false
            )
            return DayViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       val type=getItemViewType(position)
        val item=getItem(position)
        when (type) {
            HEADER -> {
                val holder=holder as CalendarViewHolder
                val model=CalendarHeaderModel()
                model.setHeaderDate(item as Long)

                holder.setViewModel(model)
            }
            EMPTY -> {
                val holder=holder as EmptyViewHolder
                val model=EmptyViewModel()
                holder.setViewModel(model)
            }
            DAY -> {
                val holder=holder as DayViewHolder
                val model=CalendarViewModel()
                    model.setCalendar(item as Calendar)
                    holder.itemView.calendarLayout.setOnClickListener{
                        click(model.mCalendar.value!!)
                    }
                if(item.get(Calendar.DAY_OF_WEEK)==1||item.get(Calendar.DAY_OF_WEEK)==7)
                    holder.itemView.dayText.setTextColor(Color.parseColor("#FF5555"))
                val find=calArray.binarySearch(model.mCalendar.value,0)
                if(find>=0){
                    val temp=findMemo(model.mCalendar.value!!)
                    holder.itemView.dailyRecyclerView.layoutManager=LinearLayoutManager(CalendarActivity.context)
                    holder.itemView.dailyRecyclerView.adapter=DailyListAdapter(temp)
                }
                holder.setViewModel(model)
            }
        }
    }
     private fun findMemo(item:Calendar):ArrayList<MemoData>{
        val tempArray=ArrayList<MemoData>()
        for(i in CalendarActivity.memoData)
            if(i.day==item.get(Calendar.DATE)&&i.month==item.get(Calendar.MONTH)&&i.year==item.get(Calendar.YEAR))
                tempArray.add(i)
        return tempArray
    }


}