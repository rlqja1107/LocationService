package com.example.test

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.ScriptGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.transfer_list_activity.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Document
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL
import kotlin.math.ceil

class ChooseTransferList : AppCompatActivity() {
    companion object {
        lateinit var context: Context
        var duration: String? = null
    }

    var distance: String? = null
    var startAddress: String? = null
    var endAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transfer_list_activity)
        backPressed.setOnClickListener {
            onBackPressed()
        }
        context = this
        selectRecyclerView.layoutManager = LinearLayoutManager(this@ChooseTransferList)
        sortSliding.panelState=SlidingUpPanelLayout.PanelState.HIDDEN
        var startLocation = intent.getParcelableExtra<LatLng>("start")
        var endLocation = intent.getParcelableExtra<LatLng>("destination")
        var url = DirectionFinder().getDirectionUrl(startLocation!!, endLocation!!)
        GetDirection(url).execute()


    }

    override fun onBackPressed() {
        if(sortSliding.panelState==SlidingUpPanelLayout.PanelState.EXPANDED||sortSliding.panelState==SlidingUpPanelLayout.PanelState.ANCHORED) {
            sortSliding.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            return
        }

        super.onBackPressed()
    }

    //공공포털을 이용해서 대중교통 환승 목록 가져오기
//    inner class UsingPublicAPI(var urlFormat:String):AsyncTask<Void,Void,Document>(){
//        override fun doInBackground(vararg p0: Void?): Document{
//            try {
//                var url = URL(urlFormat)
//                var stream = url.openStream()
//                var xpp = XmlPullParserFactory.newInstance().newPullParser()
//                xpp.setInput(InputStreamReader(stream, "UTF-8"))
//                var event=xpp.eventType
//                while(event!=XmlPullParser.END_DOCUMENT){
//                    var tag=xpp.name
//                    when(event){
//                        tag.equals()
//                    }
//                }
//            }
//            catch(e:Exception){
//                e.printStackTrace()
//            }
//
//
//        }
//
//        override fun onPostExecute(result: ArrayList<String>?) {
//
//}
//
//}
    //Google API 이용
    inner class GetDirection(var url: String) : AsyncTask<Void, Void, ArrayList<TransferData>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<TransferData> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body?.string()
            // val result = ArrayList<List<LatLng>>()
            var transferArray = ArrayList<TransferData>()
            try {

                val obj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()
                var tempObject = obj.routes[0].legs[0]
                duration = tempObject.duration.text
                distance = tempObject.distance.text
                startAddress = tempObject.start_address
                endAddress = tempObject.end_address

                for (i in 0 until obj.routes[0].legs[0].steps.size) {
                    var tempObject2 = tempObject.steps[i]
                    var start =
                        LatLng(tempObject2.start_location.lat, tempObject2.start_location.lng)

                    var end = LatLng(tempObject2.end_location.lat, tempObject2.end_location.lng)
                    transferArray.add(
                        TransferData(
                            tempObject2.distance.value,
                            (ceil(tempObject2.duration.value / 60.0)),
                            start, end, tempObject2.html_instructions,
                            tempObject2.travel_mode, tempObject2.transit_details
                        )
                    )

                    //path.addAll(decodePolyline(tempObject2.polyline.points))
                }

                //   result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return transferArray
        }

        override fun onPostExecute(result: ArrayList<TransferData>?) {

//            if (result != null) {
//                val lineoption= PolylineOptions()
//                for(i in result.indices){
//                    lineoption.addAll(result[i])
//                        .width(21f)
//                        .color(Color.BLUE)
//                        .geodesic(true)
//                }
//                //MainActivity.polyline=MainActivity.map.addPolyline(lineoption)
//            }

            StartText.text = "   ${startAddress?.removePrefix("대한민국")}"
            endText.text = "   ${endAddress?.removePrefix("대한민국")}"
            var tempArray = ArrayList<ArrayList<TransferData>>().apply {
                this.add(result!!)
            }
            var adapter = TransferAdapter(tempArray) { time ->
                var array = MainActivity.nearbyMarker
                var calculate = CalculatePrice()
                for (i in array) {
                    i.price = calculate.calculatePrice(i.company, time)
                }
                array.sortBy {
                    it.distance
                }
                selectRecyclerView.adapter = SortDataAdapter(array)
                sortSliding.panelState= SlidingUpPanelLayout.PanelState.EXPANDED
            }

            transferRecyclerview.layoutManager=LinearLayoutManager(this@ChooseTransferList)
            transferRecyclerview.adapter = adapter
        }

    }

    //Poly line을 해독
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }
        return poly
    }
}