package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.sortdata_list_sample.view.*

class SortDataViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)

class SortDataAdapter(var data:ArrayList<KickBoardData>,val click:(LatLng)->(Unit)) :RecyclerView.Adapter<SortDataViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortDataViewHolder {
        var view=LayoutInflater.from(parent.context).inflate(R.layout.sortdata_list_sample,parent,false)
        return SortDataViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SortDataViewHolder, position: Int) {
        when(data[position].company){
            "Swing"->{
                holder.itemView.kickBoardImage.setImageResource(R.drawable.swing)
            }
            "Deer"->{
                holder.itemView.kickBoardImage.setImageResource(R.drawable.deer)

            }
            "Kickgoing"->{
                holder.itemView.kickBoardImage.setImageResource(R.drawable.kickgoing)

            }
            "Beam"->{
                holder.itemView.kickBoardImage.setImageResource(R.drawable.beam)

            }
            "XingXing"->{
                holder.itemView.kickBoardImage.setImageResource(R.drawable.xingxing)
            }
        }
        holder.itemView.companyText.text=data[position].company
        holder.itemView.valueText.text="${data[position].distance.toString()}m"
        holder.itemView.choooseLayout.setOnClickListener {
            click(LatLng(data[position].marker.position.latitude,data[position].marker.position.longitude))
        }

    }
}