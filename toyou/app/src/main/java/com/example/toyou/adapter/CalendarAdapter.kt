package com.example.toyou.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import com.example.toyou.CalendarActivity
import com.example.toyou.MemoData
import com.example.toyou.R
import com.example.toyou.databinding.CalendarHeaderBinding
import com.example.toyou.databinding.DayItemBinding
import com.example.toyou.databinding.EmptyDayBinding
import com.example.toyou.ui.CalendarHeaderModel
import com.example.toyou.ui.CalendarViewModel
import com.example.toyou.ui.EmptyViewModel
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
//class CalendarAdapter2(var calArray:ArrayList<Calendar> ,var data:kotlin.collections.ArrayList<Any>,var click:(Calendar)->(Unit)):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
//    val HEADER=1
//    val EMPTY=2
//    val DAY=3
//    override fun getItemViewType(position: Int): Int {
//        var items=data[position]
//        if(items is Long)return HEADER
//        else if(items is String)return EMPTY
//        else return DAY
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//
//        if(viewType== HEADER){
//            var binding=DataBindingUtil.inflate<CalendarHeaderBinding>(LayoutInflater.from(parent.context),R.layout.item_calendar_header,parent,false)
//            var params=binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams
//            params.isFullSpan=true
//            binding.root.layoutParams=params
//            return CalendarViewHolder(binding)
//        }
//        else if(viewType== EMPTY){
//            var binding=DataBindingUtil.inflate<EmptyDayBinding>(LayoutInflater.from(parent.context),R.layout.item_day_empty,parent,false)
//            return EmptyViewHolder(binding)
//        }
//        else {
//            var binding = DataBindingUtil.inflate<DayItemBinding>(
//                LayoutInflater.from(parent.context),
//                R.layout.item_day,
//                parent,
//                false
//            )
//            return DayViewHolder(binding)
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return data.size
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        var type=getItemViewType(position)
//        var item=data[position]
//        if(type== HEADER){
//            var holder=holder as CalendarViewHolder
//            var model=CalendarHeaderModel()
//
//            if(item is Long){
//                model.setHeaderDate(item as Long)
//            }
//            holder.setViewModel(model)
//        }
//        else if(type== EMPTY){
//            var holder=holder as EmptyViewHolder
//            var model=EmptyViewModel()
//            holder.setViewModel(model)
//        }
//        else if(type== DAY){
//            var holder=holder as DayViewHolder
//            var model=CalendarViewModel()
//            if(item is Calendar){
//                model.setCalendar(item)
//                holder.itemView.calendarLayout.setOnClickListener{
//                    click(item)
//                }
//                if(item.get(Calendar.DAY_OF_WEEK)==1||item.get(Calendar.DAY_OF_WEEK)==7){
//                    holder.itemView.dayText.setTextColor(Color.parseColor("#FF5555"))
//                }
//                var find=calArray.binarySearch(item,0)
//                if(find>=0){
//                    var applyArray= findMemo(item)
//                    holder.itemView.dailyRecyclerView.layoutManager=LinearLayoutManager(CalendarActivity.context)
//                    holder.itemView.dailyRecyclerView.adapter=DailyListAdapter(applyArray)
//                }
//            }
//            holder.setViewModel(model)
//        }
//    }
//    private fun findMemo(item:Calendar):ArrayList<MemoData>{
//        val tempArray=ArrayList<MemoData>()
//        for(i in CalendarActivity.memoData)
//            if(i.day==item.get(Calendar.DATE)&&i.month==item.get(Calendar.MONTH)&&i.year==item.get(Calendar.YEAR))
//                tempArray.add(i)
//        return tempArray
//    }
//
//
//}
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
        var items=getItem(position)
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
            var binding = DataBindingUtil.inflate<DayItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_day,
                parent,
                false
            )
            return DayViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       var type=getItemViewType(position)
        var item=getItem(position)
        if(type== HEADER){
            var holder=holder as CalendarViewHolder
            var model=CalendarHeaderModel()

            if(item is Long){
                model.setHeaderDate(item as Long)
            }
            holder.setViewModel(model)
        }
        else if(type== EMPTY){
            var holder=holder as EmptyViewHolder
            var model=EmptyViewModel()
            holder.setViewModel(model)
        }
        else if(type== DAY){
            var holder=holder as DayViewHolder
            var model=CalendarViewModel()
            if(item is Calendar){
                model.setCalendar(item)
                holder.itemView.calendarLayout.setOnClickListener{
                    click(item)
                }
                var find=calArray.binarySearch(item,0)
                if(find>=0){
                    var applyArray= findMemo(item)
                    holder.itemView.dailyRecyclerView.layoutManager=LinearLayoutManager(CalendarActivity.context)
                    holder.itemView.dailyRecyclerView.adapter=DailyListAdapter(applyArray)
                }
            }
            holder.setViewModel(model)
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