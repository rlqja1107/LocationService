package com.example.test

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import com.google.gson.*
@Suppress("DEPRECATION")
class KickBoard {
    fun beamLocation(){
        object:Thread(){
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun run() {
                var http= URL("https://gateway.ridebeam.com/api/vehicles/scooter/latlong?latitude=37.504466&longitude=127.024484").openConnection()
                http=http as HttpURLConnection
                http.readTimeout=10000
                http.connectTimeout=15000
                http.requestMethod="GET"
                http.setRequestProperty("user-agent", "beam")
                http.doInput=true
                http.connect()
                var sb = StringBuffer()
                var br= InputStreamReader(http.inputStream, StandardCharsets.UTF_8) as Reader
                var kr=JsonParser().parse(br).asJsonObject
                http.disconnect()
                br.close()

                super.run()
            }
        }.start()
    }
    //Deer 위치정보 가져오기
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun deerLocation(){
            object:Thread(){
                override fun run() {
                    var http= URL("https://deering.co/carchap/deers").openConnection()
                    http=http as HttpURLConnection
                    http.readTimeout=10000
                    http.connectTimeout=15000
                    http.requestMethod="GET"
                    http.setRequestProperty("x-api-key", "carchapwemakenewmovingculture")
                    http.doInput=true
                    http.connect()
                    var sb = StringBuffer()
                    var br= BufferedReader(InputStreamReader(http.inputStream, StandardCharsets.UTF_8))
                    var line: String?
                    while (true) {
                        line = br.readLine()
                        if (line == null) break
                        sb.append(line).append("\n")
                    }
                    var array = JSONObject(sb.toString()).getJSONArray("arr")
                    for(i in 0 until array.length()-1){
                        var jb=array.getJSONObject(i)
                        MainActivity.mapKickBoard.add(KickBoardData(jb.getDouble("lat"),jb.getDouble("lng"),
                            0,jb.getInt("battery")))
                    }
                    http.disconnect()
                    br.close()

                    super.run()
                }
            }.start()
        }
        fun pushToMap(map:GoogleMap){

            for(i in MainActivity.mapKickBoard) {
                map.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            i.latitude,
                            i.longitude
                        )
                    ).title("Deer , Battery : ${i.battery}").draggable(false)
                )
            }
            }
    }

