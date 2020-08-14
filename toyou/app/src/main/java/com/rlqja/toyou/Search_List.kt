package com.rlqja.toyou


import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import io.realm.Realm
import kotlinx.android.synthetic.main.search_activity.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class Search_List : AppCompatActivity() {
    var temp = ArrayList<String>()
    var listMenu = ArrayList<listData>()
    var searchMenu = ArrayList<listData>()
    lateinit var realm: Realm
    var startOrEnd:Byte=0
    var searchList = 0
    //searchOrList가 1이면 menu에서 검색, 0이면 기존데이터베이스에서 검색
    var searchOrList = 0
    var adapter: ArrayAdapter<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.search_activity)


        setSupportActionBar(toolbar_list)
        startOrEnd=intent.getByteExtra("startOrEnd",0)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Realm.init(this)
        //창 올라오기
        //     (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY)
        //핸드폰 데이터베이스에서 검색 목록 가져옴

        realm = Realm.getDefaultInstance()
        val result = listDao(realm).getAllDemo()
        for (i in result) {
            temp.add(i.location)
            listMenu.add(i)
        }
        //temp에 목록가져오면된다.
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, temp)
        listView.adapter = adapter
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        var searchView = toolbar_list.menu.findItem(R.id.search).actionView as SearchView
        searchView.setQuery("", false)
        searchView.queryHint = "장소를 입력하세요"
        searchView.isSubmitButtonEnabled = true
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
                        var geocoder = Geocoder(this@Search_List)
                        address = geocoder.getFromLocationName(query, 3)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (address?.size!! > 0) {
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
                    } else if (searchMenu.size > 0) {
                        intentToMain(searchMenu[0])
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@Search_List, "검색 결과가 존재하지 않습니다", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                } else Toast.makeText(this@Search_List, "검색어를 입력해주세요", Toast.LENGTH_LONG).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchKeyword(newText!!)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
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

    private fun intentToMain(listData: listData) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("longitude", listData.longitude)
        intent.putExtra("latitude", listData.latitude)
        intent.putExtra("location", listData.location)
        intent.putExtra("startOrEnd",startOrEnd)
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
