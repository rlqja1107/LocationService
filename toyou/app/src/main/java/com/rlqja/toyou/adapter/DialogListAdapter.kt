package com.rlqja.toyou.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rlqja.toyou.CalendarActivity
import com.rlqja.toyou.MemoData
import com.rlqja.toyou.R
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_list_adapter_sample.view.*
class DialogViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){}
class DialogListAdapter(val dataPosition:Int,val calendarAdapter: CalendarAdapter,val data:ArrayList<MemoData>, val realm: Realm, val click:(MemoData)->Unit):RecyclerView.Adapter<DialogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        return DialogViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dialog_list_adapter_sample,parent,false))
    }

     override fun getItemCount(): Int{
         return data.size
     }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        holder.itemView.dialogTitleText.text=data[position].title
        println("Checking this out : ${data[position].title}")
        holder.itemView.clearMemo.setOnClickListener {
            val tempRemove=MemoData().deepCopy(data[position])
            CalendarActivity.removeList.add(tempRemove)
            realm.executeTransaction {
                data[position].deleteFromRealm()
            }
            calendarAdapter.notifyDataSetChanged()
           // this.notifyDataSetChanged()
        }
        holder.itemView.dialogTimeText.text=changeTime(data[position])
        holder.itemView.dialogListLayout.setOnClickListener {
            click(data[position])
        }
    }
    private fun changeTime(data:MemoData):String{
        return if(data.hour>=13){
            "Pm ${data.hour-12}시 ${data.minute}분"
        } else{
            "Am ${data.hour}시 ${data.minute}분"
        }
    }
}