package com.example.test

import java.text.SimpleDateFormat
import java.util.*

class CalculatePrice{
    fun calculatePrice(company:String,duration:Int):Int{

        var date= Date(System.currentTimeMillis())
        var format=SimpleDateFormat("hhmm")
        var time=format.format(date).toInt()
        var price=0
       when(company){
           "Swing"->{
               if(time>=2100||(time in 0..599))
                    price=1200+250*duration
               else
                   price=1200+180*duration
           }
           "Deer"->{
               price=790+150*duration
           }
           "Kickgoing"->{
               if(time in 0..399)
                   price=1500+100*duration
               else price=1000+100*duration
           }
           "Beam"->{
                price=600+180*duration
           }
           "XingXing"->{
               var day2=Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
               if(day2==7||day2==1)
                   if(time in 0..600)
                       if(duration<=5) price=2000
                       else price=2000+100*(duration-5)
                   else{
                       if(duration<=5)price=1500
                       else price=1500+100*(duration-5)
                   }
               else{
                   if(duration<=5)price=1000
                   else price=1000+(duration-5)
               }
           }
       }
        return price
    }
}