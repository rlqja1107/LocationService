package com.example.test

import android.app.SearchManager
import android.content.Context
import android.content.Intent

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.search_activity.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

class LocationSearch : AppCompatActivity() {
    lateinit var realm: Realm
    //listMenu는 데이터베이스에 저장되어 있는 목록
    var listMenu = ArrayList<listData>()
    //searchMenu는 검색시 담을 목록
    var searchMenu = ArrayList<listData>()
    var temp = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null
    //1이면 search menu에서 검색, 0이면 기존 데이터베이스에서 검색
    var searchOrList = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Realm.init(this)
        realm = Realm.getDefaultInstance()

        var result = ListDao(realm).getAllDemo()
        for (i in result) {
            listMenu.add(i)
            temp.add(i.location)
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, temp)
        listView.adapter = adapter
        //list 눌렀을 때 좌표전송
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (searchOrList == 0) intentToMain(listMenu[position])
                else {
                    realm.executeTransaction {
                        realm.copyToRealm(searchMenu[position])
                    }
                    intentToMain(searchMenu[position])
                }
            }
        removeHistory.setOnClickListener {
            realm.executeTransaction {
                result.deleteAllFromRealm()
                listMenu.clear()
                temp.clear()
                adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        var searchView = toolbar.menu.findItem(R.id.search).actionView as SearchView
        searchView.setQuery("", false)
        searchView.queryHint = "장소를 입력하세요"
        //망원경 아이콘 삭제
        searchView.isSubmitButtonEnabled = false
        searchView.maxWidth = Integer.MAX_VALUE
        var searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        //화면들어가자마자 구현되게
        searchView.onActionViewExpanded()
        //오른쪽 화살표 제거
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //geoCoder를 이용해서 위치찾기
                if (query != "" && query != null) {
                    var address: List<Address>? = null
                    try {
                        var geocoder = Geocoder(this@LocationSearch)
                        address = geocoder.getFromLocationName(query, 3)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (address?.size!!>0) {
                        var list = listData().apply {
                            this.location = query
                            this.latitude = address[0].latitude
                            this.longitude = address[0].longitude
                            this.address = address[0].countryCode
                        }
                        realm.executeTransaction {
                            it.copyToRealm(list)
                        }
                        intentToMain(list)
                    }
                    else if(searchMenu.size>0){
                        intentToMain(searchMenu[0])
                    }
                    else{
                        runOnUiThread{
                        Toast.makeText(this@LocationSearch,"검색 결과가 존재하지 않습니다",Toast.LENGTH_LONG).show()
                    }}
                }
                else Toast.makeText(this@LocationSearch, "검색어를 입력해주세요", Toast.LENGTH_LONG).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                    searchKeyword(newText!!)
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun intentToMain(listData: listData) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("longitude", listData.longitude)
        intent.putExtra("latitude", listData.latitude)
        intent.putExtra("location",listData.location)
        realm.close()
        setResult(20, intent)
        finish()

    }

    private fun searchKeyword(newText: String) {
        if (newText.length >= 2) {
            object : Thread() {
                override fun run() {
                    var url =
                        URL("https://dapi.kakao.com/v2/local/search/keyword.json?query=${newText}&size=10")
                    var http = url.openConnection() as HttpsURLConnection
                    http.requestMethod = "GET"
                    http.setRequestProperty(
                        "Authorization",
                        "KakaoAK 22e602b50f292330b9b1099d7c158846"
                    )
                    http.connect()
                    var sb = StringBuffer()
                    var br = BufferedReader(InputStreamReader(http.inputStream, "UTF-8"))
                    var line: String?
                    while (true) {
                        line = br.readLine()
                        if (line == null) break
                        sb.append(line).append("\n")
                    }
                    var jsonObject = JSONObject(sb.toString())
                    var array = jsonObject.getJSONArray("documents")
                    http.disconnect()
                    br.close()
                    temp.clear()
                    searchMenu.clear()
                    searchOrList = 1
                    if (array.length() != 0) {
                        for (i in 0 until array.length()) {
                            var jsonObject2 = array.getJSONObject(i)
                            var location = jsonObject2.getString("place_name")
                            var address = jsonObject2.getString("address_name")
                            temp.add(location)
                            searchMenu.add(listData().apply {
                                this.latitude = jsonObject2.getDouble("y")
                                this.longitude = jsonObject2.getDouble("x")
                                this.location = location
                                this.address = address
                            })
                        }
                    }
                    runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }

                    super.run()
                }
            }.start()
        }
        if (newText == "") {
            temp.clear()
            searchOrList = 0
            for (i in listMenu)
                temp.add(i.location)
            adapter?.notifyDataSetChanged()
        }

    }

}

class ListDao(private val realm: Realm) {
    fun getAllDemo(): RealmResults<listData> {
        return realm.where(listData::class.java).sort("Time", Sort.DESCENDING).findAll()
    }
}