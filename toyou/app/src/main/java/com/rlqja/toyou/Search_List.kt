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
import io.realm.FieldAttribute
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.annotations.RealmModule
import kotlinx.android.synthetic.main.search_activity.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList


class Search_List : AppCompatActivity() {
    var temp = ArrayList<String>()
    var listMenu = ArrayList<listData>()
    var searchMenu = ArrayList<listData>()
    lateinit var realm: Realm
    //searchOrList가 1이면 menu에서 검색, 0이면 기존데이터베이스에서 검색
    var searchOrList = 0
    var adapter: ArrayAdapter<String>? = null
    var distributeMemoOrCalendar=0.toByte()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.search_activity)
        //1이면 Calendar 에서 직접 추가할 때, 검색기능 활성화
        distributeMemoOrCalendar=intent.getByteExtra("distributeMemoOrCalendar",0.toByte())

        setSupportActionBar(toolbar_list)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Realm.init(this)
        //창 올라오기
        //     (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY)
        //핸드폰 데이터베이스에서 검색 목록 가져옴

        val config=RealmConfiguration.Builder().modules(RealmModuleListData()).name("search.realm").schemaVersion(4).build()
        realm = Realm.getInstance(config)
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

    override fun onBackPressed() {
        realm.close()
        super.onBackPressed()
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
        if(distributeMemoOrCalendar==1.toByte()){intent=Intent(this,MemoOnCalendar::class.java) }

        intent.putExtra("longitude", listData.longitude)
        intent.putExtra("latitude", listData.latitude)
        intent.putExtra("location", listData.location)
        realm.close()
        setResult(20, intent)
        finish()
    }

    private fun searchKeyword(newText: String) {
        if (newText.length >= 2) {
            object : Thread() {
                override fun run() {
                    val url =
                        URL("https://dapi.kakao.com/v2/local/search/keyword.json?query=${newText}&size=10")
                    val http = url.openConnection() as HttpsURLConnection
                    http.requestMethod = "GET"
                    http.setRequestProperty(
                        "Authorization",
                        "KakaoAK 22e602b50f292330b9b1099d7c158846"
                    )
                    http.connect()
                    val sb = StringBuffer()
                    val br = BufferedReader(InputStreamReader(http.inputStream, "UTF-8"))
                    var line: String?
                    while (true) {
                        line = br.readLine()
                        if (line == null) break
                        sb.append(line).append("\n")
                    }
                    val jsonObject = JSONObject(sb.toString())
                    val array = jsonObject.getJSONArray("documents")
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
    @RealmModule(classes=[listData::class])
    inner class RealmModuleListData


}
