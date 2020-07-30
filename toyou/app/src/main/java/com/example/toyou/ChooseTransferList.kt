package com.example.toyou

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.naver.maps.geometry.LatLng
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.transfer_list_activity.*
import okhttp3.OkHttpClient
import okhttp3.Request
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
        var transferPass: LatLng? = null
    }

    var distance: String? = null
    var startAddress: String? = null
    var endAddress: String? = null
    var destination: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transfer_list_activity)
        backPressed.setOnClickListener {
            onBackPressed()
        }
        context = this
        selectRecyclerView.layoutManager = LinearLayoutManager(this@ChooseTransferList)
        sortSliding.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        var startLocation = intent.getParcelableExtra<LatLng>("start")
        var endLocation = intent.getParcelableExtra<LatLng>("destination")
        StartText.text = "   ${DirectionFinder().convertToAddress(
            startLocation.latitude,
            startLocation.longitude
        )}"
        endText.text =
            "   ${DirectionFinder().convertToAddress(endLocation.latitude, endLocation.longitude)}"
        var url = DirectionFinder().getPublicDataUrl(startLocation, endLocation)
        UsingPublicAPI(url).execute()
    }

    override fun onBackPressed() {
        if (sortSliding.panelState == SlidingUpPanelLayout.PanelState.EXPANDED || sortSliding.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {
            sortSliding.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            return
        }
        super.onBackPressed()
    }

    //  공공포털을 이용해서 대중교통 환승 목록 가져오기
    inner class UsingPublicAPI(var urlFormat: String) :
        AsyncTask<Void, Void, ArrayList<TransitArray>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<TransitArray> {
            var transitList = ArrayList<TransitArray>()
            try {
                var url = URL(urlFormat)
                var stream = url.openStream()
                var xpp = XmlPullParserFactory.newInstance().newPullParser()
                xpp.setInput(InputStreamReader(stream, "UTF-8"))
                var event = xpp.eventType
                var transit = TransitArray(ArrayList<TransitData>(), 0, 0)
                var data = TransitData()
                var count = 0
                var transferCount = 0
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (count == 4) break

                    if (event == XmlPullParser.START_TAG) {
                        var tag = xpp.name
                        when (tag) {
                            "pathList"->{
                                data=TransitData()
                            }
                            "fname" -> {
                                xpp.next()
                                data.startName = xpp.text
                            }
                            "fy" -> {
                                xpp.next()
                                data.startX = xpp.text.toDouble()
                            }
                            "fx" -> {
                                xpp.next()
                                data.startY = xpp.text.toDouble()
                            }
                            "routeNm" -> {
                                xpp.next()
                                data.routeNum = xpp.text
                            }
                            "tname" -> {
                                xpp.next()
                                data.endName = xpp.text.toString()
                            }
                            "tx" -> {
                                xpp.next()
                                data.endY = xpp.text.toDouble()
                            }
                            "ty" -> {
                                xpp.next()
                                data.endX = xpp.text.toDouble()
                                data.stationNum = transferCount
                                transit.dataList.add(data)
                                transferCount = 0
                            }
                            "railLinkList" -> {
                                transferCount++
                            }
                            "distance" -> {
                                xpp.next()
                                transit=TransitArray(ArrayList(),1,0)
                                transit.distance = xpp.text.toInt()
                                println("distance: ${transit.distance}")
                            }
                            "time" -> {
                                xpp.next()
                                transit.time = xpp.text.toInt()
                                transitList.add(transit)
                                count++
                            }
                            else -> {
                            }
                        }
                    }
                    event = xpp.next()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return transitList
        }

        override fun onPostExecute(result: ArrayList<TransitArray>?) {
            if (result != null) {
                transferRecyclerview.layoutManager = LinearLayoutManager(this@ChooseTransferList)
                transferRecyclerview.adapter = TransferAdapter(result) {

                }
            }

        }


    }

    //Google API 이용
    inner class GetDirection(var url: String) : AsyncTask<Void, Void, ArrayList<TransferData>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<TransferData> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()
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
                    if (i == 0) destination = end
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

//            StartText.text = "   ${startAddress?.removePrefix("대한민국")}"
//            endText.text = "   ${endAddress?.removePrefix("대한민국")}"
//            var tempArray = ArrayList<ArrayList<TransferData>>().apply {
//                this.add(result!!)
//            }
//            var adapter = TransferAdapter(tempArray) {time ->
//                var array = MainActivity.nearbyMarker
//                var calculate = CalculatePrice()
//                for (i in array){
//                    i.price = calculate.calculatePrice(i.company, time)
//                }
//                array.sortBy{
//                    it.distance
//                }
//                selectRecyclerView.adapter = SortDataAdapter(array){
//                    transferPass=it
//                    var intent= Intent(this@ChooseTransferList,MainActivity::class.java)
//                    intent.putExtra("position",it)
//                    intent.putExtra("destination",destination)
//                    setResult(5,intent)
//                    finish()
//                }
//                sortSliding.panelState= SlidingUpPanelLayout.PanelState.EXPANDED
//            }
//            transferRecyclerview.layoutManager=LinearLayoutManager(this@ChooseTransferList)
//            transferRecyclerview.adapter = adapter
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