package com.example.toyou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transfer_list_inner_sample.view.*
import kotlinx.android.synthetic.main.transfer_list_sample.view.*
import org.jetbrains.anko.wrapContent

class TransferViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)
class TransferAdapter(var data:ArrayList<TransitArray>,var itemClick:()->(Unit)):RecyclerView.Adapter<TransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        return TransferViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transfer_list_sample,parent,false))
    }

    override fun getItemCount(): Int {
        return data.size

    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        if(data[position].time>60) {
            holder.itemView.timeText.text = "${data[position].time/60}시간 ${data[position].time%60}분"
        }
        else holder.itemView.timeText.text="${data[position].time}분"
        holder.itemView.distanceText.text="${data[position].distance/1000.0}km"
        var manager=LinearLayoutManager(ChooseTransferList.context)
        manager.orientation=LinearLayoutManager.HORIZONTAL
        holder.itemView.transferlist.layoutManager=manager
        holder.itemView.transferlist.adapter=TransferInnerAdapter(data[position].dataList,holder.itemView.transferlist.width)
    }
}

class TransferInnerAdapter(var data:ArrayList<TransitData>,var width:Int):RecyclerView.Adapter<TransferViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        return TransferViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transfer_list_inner_sample,parent,false))
    }

    override fun getItemCount(): Int {
       return data.size
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        //holder.itemView.innerLayout.layoutParams=LinearLayout.LayoutParams(width/data.size,74)
       // holder.itemView.outerLayout.layoutParams=ConstraintLayout.LayoutParams(width/data.size,74)

        if(data[position].stationNum==0) {
            holder.itemView.transferImage.setImageResource(R.drawable.bus)
            holder.itemView.directionImage.setImageResource(R.drawable.pink_direction)
        }
        else{ holder.itemView.transferImage.setImageResource(R.drawable.subway)
            holder.itemView.directionImage.setImageResource(R.drawable.brown_direction)
        }
       // holder.itemView.transferImage.layout((width/data.size-48)/2,0,(width/data.size-48)/2,0)
        //holder.itemView.transferImage.setPadding((width/data.size-48)/2,0,(width/data.size-48)/2,0)
        holder.itemView.routeNum.text=data[position].startName
        //holder.itemView.routeNum.layoutParams=ConstraintLayout.LayoutParams(width/data.size, wrapContent)
        holder.itemView.lineNum.text=data[position].routeNum
    }

}