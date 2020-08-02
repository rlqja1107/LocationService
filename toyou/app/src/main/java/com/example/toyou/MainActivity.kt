package com.example.toyou


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.FieldAttributes
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.realm.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_sliding_inner.*
import kotlinx.android.synthetic.main.main_sliding_inner.view.*
import kotlinx.android.synthetic.main.transfer_list_activity.view.*
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback {
    companion object {
        var context: Context? = null
        var init_latitude: Double? = null
        var init_longitude: Double? = null
    }

    private lateinit var mapView: MapView
    lateinit var locationSource: FusedLocationSource
    var markerCount = 0
    var currentFlag = 0
    var endLocation: LatLng? = null
    var startLocation: LatLng? = null
    var uniqueMarker1: Marker? = null
    var uniqueMarker2: Marker? = null
    lateinit var manager: LocationManager
    private var naverMap: NaverMap? = null
    var memo = MemoData()
    var realm: Realm? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        deleteButton.visibility=View.INVISIBLE
        Realm.init(this)
        var config=RealmConfiguration.Builder().migration { realm, oldVersion, newVersion ->
            var schema=realm.schema
            schema.create("MemoData").addField("id",String::class.java,FieldAttribute.PRIMARY_KEY,FieldAttribute.REQUIRED)
                .addField("latitude",Double::class.java)
                .addField("longitude",Double::class.java)
                .addField("contents",String::class.java,FieldAttribute.REQUIRED)
                .addField("title",String::class.java,FieldAttribute.REQUIRED)
                .addField("year",Int::class.java)
                .addField("month",Int::class.java)
                .addField("day",Int::class.java)
                .addField("hour",Int::class.java)
                .addField("minute",Int::class.java)
        }.schemaVersion(1).build()
        Realm.setDefaultConfiguration(config)
        realm=Realm.getDefaultInstance()



        var memodata = MemoClass(realm!!).getAllMemo()

        var cal = GregorianCalendar()
        val nYear = cal.get(Calendar.YEAR)
        val nMonth = cal.get(Calendar.MONTH)
        val nDay = cal.get(Calendar.DAY_OF_MONTH)
        val nHour = cal.get(Calendar.HOUR_OF_DAY)
        val nMinute = cal.get(Calendar.MINUTE)
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // toolbar.setTitleTextColor(Color.parseColor("#ffffff"))
        setSupportActionBar(toolbar)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)

        toolbar.setOnClickListener {
            var searchIntent = Intent(this, Search_List::class.java)
            startActivityForResult(searchIntent, 20)
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // locationSource=FusedLocationSource(this,PERMISSION_LOCATION_CODE)
        init_latitude = intent.getDoubleExtra("latitude", 37.56398)
        init_longitude = intent.getDoubleExtra("longitude", 126.97935)
        var checking = intent.getIntExtra("checkingPermission", 100)
        toolbar.setOnClickListener {
            var localActivity = Intent(this, Search_List::class.java)
            startActivityForResult(localActivity, 20)
        }
        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN

        //권한이 체크되어있는지, 안되어있으면 권한 요청
        if (!checkPermission()) {
            requestPermission()
        }
        //GPS, NetWork 체크
        //체크 안되어있으면 snackbar로 권한 체크 유도
        if (checking == 0) {
            Snackbar.make(
                Main_drawer,
                "폰의 위치기능을 켜야 사용할 수 있습니다.",
                10000
            ).setAction("설정", View.OnClickListener {
                val goSetting = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(goSetting, 100)
            }).show()
            initSetting(mapView, init_latitude, init_longitude, 0,memodata)
        } else {
            initSetting(mapView, init_latitude, init_longitude, 1,memodata)
        }
        //핸드폰 데이터베이스로부터 마커 불러와서 찍히게하기

        //현재 위치로 이동
        currentLocation.setOnClickListener {
            moveCamera(init_latitude, init_longitude)
            if (currentFlag == 0) putMarker(init_latitude, init_longitude)
            currentFlag = 1
        }

        bottom_slide.timerImage.setOnClickListener {
            var date = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    memo.year = year
                    memo.month = month
                    memo.day = dayOfMonth
                    bottom_slide.timeText.text="${memo.year}년 ${memo.month}월${memo.day}일 ${memo.hour}시 ${memo.minute}분"
                }, nYear, nMonth, nDay
            )
            date.setTitle("시간 정하기")
            date.show()
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                memo.hour = hour
                memo.minute = minute

            }, nHour, nMinute, false).show()

        }


    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == 10) {
            moveCamera(init_latitude, init_longitude)
        }
        //위치검색 후 결과
        if (requestCode == 20 && resultCode == 20) {
            var latitude = data?.getDoubleExtra("latitude", init_latitude!!)
            var longitude = data?.getDoubleExtra("longitude", init_longitude!!)
            var location = data?.getStringExtra("location")
            moveCamera(latitude, longitude)
            var mark = putMarker(latitude, longitude)
            showPanel(latitude!!, longitude!!, false, null, mark)

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            }
            naverMap?.locationTrackingMode = LocationTrackingMode.Face
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //Drawer에서 목록을 골랐을 때
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        Main_drawer.closeDrawers()
        when (item.itemId) {
            R.id.Login -> {

            }
            R.id.Mypage -> {

            }
            R.id.Setting -> {

            }
            R.id.Notification -> {

            }
        }

        Main_drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.first_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (slidingPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED || slidingPanel.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if (slidingPanel.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            return
        }
        if (Main_drawer.isDrawerOpen(GravityCompat.START)) {
            Main_drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //toolbar에 있는 menu 행동을 정함
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {//왼쪽 상단 버튼을 눌렀을 때
                Main_drawer.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //현재 위치의 위도경도를 알려주고 mapView 적용
    fun CurrentLocation(manager: LocationManager, mapView: MapView) {
        val criteria = Criteria()
        //정확도 체크
        criteria.accuracy = Criteria.ACCURACY_MEDIUM
        criteria.powerRequirement = Criteria.POWER_MEDIUM
        if (checkPermission()) {
            manager.requestSingleUpdate(criteria, object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    init_latitude = location?.latitude
                    init_longitude = location?.longitude
//                initSetting(mapView,location?.latitude,location?.longitude)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }

                override fun onProviderEnabled(provider: String?) {
                }

                override fun onProviderDisabled(provider: String?) {
                }
            }, null)
        } else Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_LONG).show()
    }
    //위치 권한 사용 체크

    //카메라 움직이기
    private fun moveCamera(latitude: Double?, longitude: Double?) {
        val camerUpdate =
            CameraUpdate.scrollTo(LatLng(latitude!!, longitude!!)).animate(CameraAnimation.Fly)
        val cameraposition = CameraPosition(LatLng(latitude, longitude), 16.0)
        naverMap?.cameraPosition = cameraposition
        naverMap?.moveCamera(camerUpdate)
    }

    private fun initSetting(
        mapView: MapView,
        latitude: Double?,
        longitude: Double?,
        checking: Int,
        memoData: RealmResults<MemoData>
    ) {

        //지도의 특성 변경, 길게 눌렀을 때 실행방법
        mapView.getMapAsync {
            naverMap = it
            for(i in memoData){
                initMarkerSetting(i)
            }
            if (checking == 1) {
                val locationLay = it.locationOverlay
                locationLay.isVisible = true
                locationLay.iconWidth = LocationOverlay.SIZE_AUTO
                locationLay.iconHeight = LocationOverlay.SIZE_AUTO
                locationLay.position = LatLng(latitude!!, longitude!!)
                locationLay.circleRadius = 200
                it.locationTrackingMode = LocationTrackingMode.Face
            }
            it.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            it.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
            it.setOnMapLongClickListener { pointF, latLng ->
                memo.latitude = latLng.latitude
                memo.longitude = latLng.longitude
                var mark = putMarker(latLng.latitude, latLng.longitude)
                showPanel(latLng.latitude, latLng.longitude, false, null, mark)
                false
            }

            moveCamera(init_latitude, init_longitude)
        }
        map_View.addView(mapView)
    }

    //마커 기본특성
    private fun markerProperty(marker: Marker, latLng: LatLng): Marker {
        marker.position = LatLng(latLng.latitude, latLng.longitude)
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        marker.isCaptionPerspectiveEnabled = true
        marker.setOnClickListener {
            showPanel(latLng.latitude, latLng.longitude, false, null, marker)
            var dialog = AlertDialog.Builder(this)
            dialog.setPositiveButton("지우기") { dia, _ ->
                if (currentFlag == 1) currentFlag = 0
                marker.map = null
                markerCount--
            }
            dialog.setNegativeButton("확인") { dia, _ ->
                dia.dismiss()
            }
            //dialog.show()
            true
        }
        var info = InfoWindow()
        info.adapter = object : InfoWindow.DefaultTextAdapter(this) {
            override fun getText(p0: InfoWindow): CharSequence {
                return "우리집 강아지!"
            }
        }
        info.open(marker)
        marker.icon = MarkerIcons.LIGHTBLUE
        return marker
    }

    private fun initMarkerSetting(data: MemoData) {
        var marker = Marker()
        marker.position = LatLng(data.latitude, data.longitude)
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        marker.isCaptionPerspectiveEnabled = true
        marker.setOnClickListener {
            showPanel(data.latitude, data.longitude, true, data, marker)
            true
        }
        var info = InfoWindow()
        info.adapter = object : InfoWindow.DefaultTextAdapter(this) {
            override fun getText(p0: InfoWindow): CharSequence {
                return title
            }
        }
        info.open(marker)
        marker.icon = MarkerIcons.GRAY
        marker.map = naverMap
    }

    var total = 0
    private fun putMarker(latitude: Double?, longitude: Double?): Marker {
        if (markerCount == 2) {
            if (total % 2 == 0) {
                uniqueMarker1?.map = null
                uniqueMarker1 = null
            } else {
                uniqueMarker2?.map = null
                uniqueMarker2 = null

            }
            total++
            markerCount--
        }
        var marker = Marker()
        marker = markerProperty(marker, LatLng(latitude!!, longitude!!))
        if (uniqueMarker1 == null) {

            marker.map = naverMap
            uniqueMarker1 = marker
            markerCount++
            return uniqueMarker1!!
        } else if (uniqueMarker2 == null) {
            marker.map = naverMap
            uniqueMarker2 = marker
            markerCount++
            return uniqueMarker2!!
        }
        return uniqueMarker1!!

    }

    //추적 불가능
    private fun checkPermission(): Boolean {
        val finePermission =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        val coarsePermission =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        return finePermission && coarsePermission
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
            Toast.makeText(this, "앱을 실행시키려면 위치 권한이 필요합니다", Toast.LENGTH_LONG).show()
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), 200
        )
    }
    private fun confirmButtonClick(saveMarker: Marker){
        bottom_slide.titleText.isClickable = true
        bottom_slide.timerImage.isClickable = true
        bottom_slide.contentText.isClickable = true
        bottom_slide.confirmButton.setOnClickListener {
            when {
                bottom_slide.titleText.text.toString() == "" -> Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                memo.year == 0 -> Toast.makeText(this, "시간을 설정해주세요", Toast.LENGTH_SHORT).show()
                memo.latitude == 0.0 -> Toast.makeText(
                    this,
                    "위치를 설정해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {

                    saveMarker.icon = MarkerIcons.GRAY
                    saveMarker.map = naverMap
                    memo.title=bottom_slide.titleText.text.toString()
                    memo.latitude=saveMarker.position.latitude
                    memo.longitude=saveMarker.position.longitude
                    memo.contents=bottom_slide.contentText.text.toString()
                    realm?.executeTransaction {
                    it.copyToRealm(memo)
                }
                    Toast.makeText(this, "Toyou 메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

        }
        confirmButton.text = "저장"
    }
    private fun showPanel(latitude: Double, longitude: Double, touchFlag: Boolean,data:MemoData?,marker:Marker?) {
        if (touchFlag) {
            deleteButton.visibility=View.VISIBLE
            deleteButton.setOnClickListener {
                realm?.executeTransaction {
                    if(data!=null){
                        data?.deleteFromRealm()
                        Toast.makeText(this,"삭제되었습니다",Toast.LENGTH_SHORT).show()
                    }
                    else Toast.makeText(this, "삭제 실패",Toast.LENGTH_SHORT).show()
                }
                marker?.map=null
                bottom_slide.titleText.setText("")
                bottom_slide.contentText.setText("")
                bottom_slide.timeText.text = "시간 설정해주세요"
                bottom_slide.deleteButton.visibility=View.INVISIBLE
            }
            bottom_slide.timeText.text="${data?.year}년 ${data?.month}월${data?.day}일 ${data?.hour}시${data?.minute}분"
            bottom_slide.titleText.setText(data?.title.toString())
            bottom_slide.contentText.setText(data?.contents.toString())
            bottom_slide.titleText.isClickable = false
            bottom_slide.timerImage.isClickable = false
            bottom_slide.contentText.isClickable = false
            confirmButton.text = "수정"
            bottom_slide.confirmButton.setOnClickListener {
                confirmButtonClick(marker!!)
            }
        } else {
            deleteButton.visibility=View.INVISIBLE
            bottom_slide.titleText.setText("")
            bottom_slide.contentText.setText("")
            bottom_slide.timeText.text = "시간 설정해주세요"
            confirmButtonClick(marker!!)
        }

        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        bottom_slide.addressText.text = DirectionFinder().convertToAddress(latitude, longitude)
        bottom_slide.endButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "목적지점이 지정되었습니다", Toast.LENGTH_SHORT).show()
            endLocation = LatLng(latitude, longitude)
        }
        bottom_slide.startButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "출발지점이 지정되었습니다", Toast.LENGTH_SHORT).show()
            startLocation = LatLng(latitude, longitude)
        }
        bottom_slide.findWay.setOnClickListener {
            if (endLocation == null || startLocation == null)
                Toast.makeText(
                    this@MainActivity,
                    "출발 지점 또는 목적지점을 지정하십시오",
                    Toast.LENGTH_SHORT
                ).show()
            else {

                val intentToList = Intent(this, ChooseTransferList::class.java)
                intentToList.putExtra("start", startLocation)
                intentToList.putExtra("destination", endLocation)
                startActivityForResult(intentToList, 5)
            }
        }
        moveCamera(latitude, longitude)
    }


}




