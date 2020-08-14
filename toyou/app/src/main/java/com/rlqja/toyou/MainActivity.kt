package com.rlqja.toyou


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.link.LinkClient
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
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        var context: Context? = null
        var init_latitude: Double? = null
        var init_longitude: Double? = null
    }

    var startText: String? = null
    var endText: String? = null
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
    var memodata: RealmResults<MemoData>? = null
    lateinit var currentMemoArray:ArrayList<MemoData>
    var startFlag: Byte = 0
    var mode: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KakaoSdk.init(this,resources.getString(R.string.kakao_appkey))
        context = this
        currentMemoArray=ArrayList<MemoData>()
        deleteButton.visibility = View.INVISIBLE
        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        destImage.visibility = View.INVISIBLE
        destinationText.visibility = View.INVISIBLE
        switchImage.visibility = View.INVISIBLE
        deleteDestImage.visibility = View.INVISIBLE
        slidingPanel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                slidingPanel.isTouchEnabled = newState != SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        })
        Realm.init(this)
        val config = RealmConfiguration.Builder().migration { realm, oldVersion, newVersion ->
            val schema = realm.schema
            schema.create("MemoData").addField(
                "id",
                String::class.java,
                FieldAttribute.PRIMARY_KEY,
                FieldAttribute.REQUIRED
            )
                .addField("latitude", Double::class.java)
                .addField("longitude", Double::class.java)
                .addField("contents", String::class.java, FieldAttribute.REQUIRED)
                .addField("title", String::class.java, FieldAttribute.REQUIRED)
                .addField("year", Int::class.java)
                .addField("month", Int::class.java)
                .addField("day", Int::class.java)
                .addField("hour", Int::class.java)
                .addField("minute", Int::class.java)
                .addField("memoOrCalendar", Boolean::class.java)
        }.schemaVersion(1).build()
       // Realm.setDefaultConfiguration(config)
        realm = Realm.getDefaultInstance()

        memodata = MemoClass(realm!!).getAllMemo()

        val cal = GregorianCalendar()
        val nYear = cal.get(Calendar.YEAR)
        val nMonth = cal.get(Calendar.MONTH)
        val nDay = cal.get(Calendar.DAY_OF_MONTH)
        val nHour = cal.get(Calendar.HOUR_OF_DAY)
        val nMinute = cal.get(Calendar.MINUTE)
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setSupportActionBar(toolbar)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        locationSource = FusedLocationSource(this, 10)
        init_latitude = intent.getDoubleExtra("latitude", 37.56398)
        init_longitude = intent.getDoubleExtra("longitude", 126.97935)
        val checking = intent.getIntExtra("checkingPermission", 100)
        toolbar.setOnClickListener {
            val localActivity = Intent(this, Search_List::class.java)
            localActivity.putExtra("startOrEnd", startFlag)
            startActivityForResult(localActivity, 20)
        }
        destImage.setOnClickListener {
            val localActivity = Intent(this, Search_List::class.java)
            localActivity.putExtra("startOrEnd", 1)
            startActivityForResult(localActivity, 20)
        }
        switchImage.setOnClickListener {
            val tempText = showTitle.text
            showTitle.text = destinationText.text
            destinationText.text = tempText
            val tempLocation = startLocation
            startLocation = endLocation
            endLocation = startLocation
        }
        when (checking) {
            2 -> {
                requestPermission()
                requestLocation()
                initSetting(mapView, init_latitude, init_longitude, 0, memodata!!)
            }
            0 -> {
                requestPermission()
                initSetting(mapView, init_latitude, init_longitude, 0, memodata!!)
            }
            1 -> {
                requestLocation()
                initSetting(mapView, init_latitude, init_longitude, 0, memodata!!)
            }
            else -> {
                initSetting(mapView, init_latitude, init_longitude, 1, memodata!!)
            }
        }
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
                    bottom_slide.timeText.text =
                        "${memo.year}년 ${memo.month + 1}월${memo.day}일 ${memo.hour}시 ${memo.minute}분"
                }, nYear, nMonth, nDay
            )
            date.setTitle("시간 정하기")
            date.show()
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                memo.hour = hour
                memo.minute = minute

            }, nHour, nMinute, false).show()
        }
        calendarImage.setOnClickListener {
            var intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        bottom_slide.calendar.setOnClickListener {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            with(bottom_slide) {
                timerImage.visibility = View.VISIBLE
                timeText.visibility = View.VISIBLE
            }
            mode = true
        }
        bottom_slide.memoButton.setOnClickListener {
            with(bottom_slide) {
                timerImage.visibility = View.GONE
                timeText.visibility = View.GONE
            }
            mode = false
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        navigation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Login -> {
                }
                R.id.Mypage -> {
                }
                R.id.Setting-> {
                    LinkClient.instance.customTemplate(this,34322){linkResult, error ->
                        if(error!=null)Toast.makeText(this,"카카오링크보내기 실패",Toast.LENGTH_SHORT).show()
                        else if(linkResult!=null){
                            startActivity(linkResult.intent)
                        }
                    }
                }
                R.id.Notification -> {
                }
            }
            Main_drawer.closeDrawers()
            true
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
        //대중교통 목록을 검색후
        else if(requestCode==5&&resultCode==5){
            startLocation=null
            endLocation=null
        }
        //위치검색 후 결과
        else if (requestCode == 20 && resultCode == 20) {
            val latitude = data?.getDoubleExtra("latitude", init_latitude!!)
            val longitude = data?.getDoubleExtra("longitude", init_longitude!!)
            val startOrEnd = data?.getByteExtra("startOrEnd", 0)
            if (startOrEnd == 0.toByte()) {
                showTitle.text = data.getStringExtra("location")
            } else if (startOrEnd == 1.toByte()) {
                destinationText.text = data?.getStringExtra("location")
                endText = destinationText.text.toString()
                endLocation = LatLng(latitude!!, longitude!!)
            } else {
                startLocation = LatLng(latitude!!, longitude!!)
                showTitle.text = data?.getStringExtra("location")
                startText = showTitle.text.toString()
            }
            moveCamera(latitude, longitude)
            val mark = putMarker(latitude, longitude)
            showPanel(latitude!!, longitude!!, false, null, mark,false)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 200) {
            if (!loading_activity().checkPermission()) {
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            } else {
                naverMap?.locationTrackingMode = LocationTrackingMode.Face
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_MEDIUM
                criteria.powerRequirement = Criteria.POWER_MEDIUM

                manager.requestSingleUpdate(criteria, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        init_latitude = location?.latitude
                        init_longitude = location?.longitude
                        overlaySetting(init_latitude!!, init_longitude!!)
                        moveCamera(init_latitude, init_longitude)
                    }

                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?
                    ) {
                    }

                    override fun onProviderEnabled(provider: String?) {
                    }

                    override fun onProviderDisabled(provider: String?) {
                    }
                }, null)
            }

            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.first_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (slidingPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
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

    //카메라 움직이기
    private fun moveCamera(latitude: Double?, longitude: Double?) {
        val camerUpdate =
            CameraUpdate.scrollTo(LatLng(latitude!!, longitude!!)).animate(CameraAnimation.Fly)
        val cameraposition = CameraPosition(LatLng(latitude, longitude), 16.0)
        naverMap?.cameraPosition = cameraposition
        naverMap?.moveCamera(camerUpdate)
    }

    private fun overlaySetting(latitude: Double, longitude: Double) {
        val locationLay = naverMap?.locationOverlay
        locationLay?.isVisible = true
        locationLay?.iconWidth = LocationOverlay.SIZE_AUTO
        locationLay?.iconHeight = LocationOverlay.SIZE_AUTO
        locationLay?.position = LatLng(latitude, longitude)
        locationLay?.circleRadius = 200
        naverMap?.locationTrackingMode = LocationTrackingMode.Face
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
            for (i in memoData) {
                initMarkerSetting(i)
            }
            if (checking == 1) {
                overlaySetting(latitude!!, longitude!!)
            }
            it.setOnMapLongClickListener { pointF, latLng ->
                val mark = putMarker(latLng.latitude, latLng.longitude)
                if (destinationText.visibility != View.VISIBLE)
                    showTitle.text =
                        DirectionFinder().convertToAddress(latLng.latitude, latLng.longitude)
                showPanel(latLng.latitude, latLng.longitude, false, null, mark,false)
            }
            it.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            it.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
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
            showPanel(latLng.latitude, latLng.longitude, false, null, marker,false)
            true
        }
        val info = InfoWindow()
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

        with(marker) {
            position = LatLng(data.latitude, data.longitude)
            width = Marker.SIZE_AUTO
            height = Marker.SIZE_AUTO
            isCaptionPerspectiveEnabled = true
            setOnClickListener {
                showPanel(data.latitude, data.longitude, true, data, marker,data.memoOrCalendar)

                true
            }
            setCaptionAligns(Align.Top)
            map = naverMap
            if (data.memoOrCalendar) {
                icon = OverlayImage.fromResource(R.drawable.calendarpin)
                captionText = "${data.month+1}월 ${data.day}일"
            } else {
                icon = OverlayImage.fromResource(R.drawable.memopin)
                captionText=data.title
            }
        }

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

    private fun requestLocation() {
        Snackbar.make(
            Main_drawer,
            "폰의 위치기능을 켜야 사용할 수 있습니다.",
            30000
        ).setAction("설정", View.OnClickListener {
            val goSetting = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(goSetting, 100)
        }).show()
    }

    private fun confirmButtonClick(saveMarker: Marker) {
        bottom_slide.titleText.isClickable = true
        bottom_slide.timerImage.isClickable = true
        bottom_slide.contentText.isClickable = true
        bottom_slide.confirmButton.setOnClickListener {
            when {
                bottom_slide.titleText.text.toString() == "" -> Toast.makeText(
                    this,
                    "제목을 입력해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                (memo.year == 0&&mode) -> Toast.makeText(this, "시간을 설정해주세요", Toast.LENGTH_SHORT).show()
                memo.latitude == 0.0 -> Toast.makeText(
                    this,
                    "위치를 설정해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {

                    with(memo) {
                        title = bottom_slide.titleText.text.toString()
                        latitude = saveMarker.position.latitude
                        longitude = saveMarker.position.longitude
                        contents = bottom_slide.contentText.text.toString()
                        memo.memoOrCalendar = mode
                    }
                    val tempMemo=MemoData().deepCopy(memo)
                    with(saveMarker) {
                        if (mode) {
                            icon = OverlayImage.fromResource(R.drawable.calendarpin)
                            captionText = "${memo.month}월 ${memo.day}일"
                        } else {
                            icon = OverlayImage.fromResource(R.drawable.memopin)
                            captionText = bottom_slide.titleText.text.toString()
                        }
                        setOnClickListener {
                            showPanel(this.position.latitude,this.position.longitude,true,tempMemo,this,tempMemo.memoOrCalendar)
                            false
                        }
                        captionRequestedWidth = 150
                        setCaptionAligns(Align.Top)
                    }
                    uniqueMarker1= null
                    uniqueMarker2=null
                    markerCount=0

                    realm?.executeTransaction {
                        it.copyToRealm(memo)
                    }
                    Toast.makeText(this, "Toyou 메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    memo = MemoData()
                }
            }
        }
        confirmButton.text = "저장"
    }

    private fun showPanel(
        latitude: Double,
        longitude: Double,
        touchFlag: Boolean,
        data: MemoData?,
        marker: Marker?,
        memoOrCalendar:Boolean
    ) {

        if (touchFlag) {
            if(memoOrCalendar){
                bottom_slide.timerImage.visibility=View.VISIBLE
                bottom_slide.timeText.visibility=View.VISIBLE
            }
            else{
                bottom_slide.timerImage.visibility=View.INVISIBLE
                bottom_slide.timeText.visibility=View.INVISIBLE
            }
            bottom_slide.memoButton.visibility=View.INVISIBLE
            bottom_slide.calendar.visibility=View.INVISIBLE
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                realm?.executeTransaction {
                    if (data != null) {
                            val removedata= it.where(MemoData::class.java).equalTo("id",data.id).findFirst()
                        removedata?.deleteFromRealm()
                        Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
                marker?.map = null
                with(bottom_slide) {
                    titleText.setText("")
                    contentText.setText("")
                    timeText.text = "시간 설정해주세요"
                    deleteButton.visibility = View.INVISIBLE
                }
                slidingPanel.panelState=SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            with(bottom_slide) {
                if (data != null) {
                    timeText.text =
                        "${data.year}년 ${data.month+1}월${data.day}일 ${data.hour}시${data.minute}분"
                }
                titleText.setText(data?.title.toString())
                contentText.setText(data?.contents.toString())
                titleText.isClickable = false
                timerImage.isClickable = false
                contentText.isClickable = false
                confirmButton.setOnClickListener {
                    reviseMarkerInfo(data!!,marker!!)
                }
            }
            confirmButton.text = "수정"
        } else {
            bottom_slide.memoButton.visibility=View.VISIBLE
            bottom_slide.calendar.visibility=View.VISIBLE
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            deleteButton.visibility = View.INVISIBLE
            bottom_slide.titleText.setText("")
            bottom_slide.contentText.setText("")
            bottom_slide.timeText.text = "시간 설정해주세요"
            confirmButtonClick(marker!!)
            memo.latitude = latitude
            memo.longitude = longitude
        }

        bottom_slide.endButton.setOnClickListener {
            endLocation = LatLng(latitude, longitude)
            viewDestinationOnMap(startLocation, "목적지점이 지정되었습니다.")
            destinationText.text = DirectionFinder().convertToAddress(latitude, longitude)
            if (showTitle.text.toString() == "장소 및 위치 검색하기")
                showTitle.text = "출발지점을 지정하세요"
        }
        bottom_slide.startButton.setOnClickListener {
            startLocation = LatLng(latitude, longitude)
            showTitle.text = DirectionFinder().convertToAddress(latitude, longitude)
            viewDestinationOnMap(endLocation, "출발지점이 지정되었습니다.")
        }
        moveCamera(latitude, longitude)
    }

    private fun viewDestinationOnMap(which: LatLng?, text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        startFlag = 1
        destImage.visibility = View.VISIBLE
        destinationText.visibility = View.VISIBLE
        switchImage.visibility = View.VISIBLE
        deleteDestImage.visibility = View.VISIBLE
        if (which != null) {
            goToTransferActivity()
        }
    }

    private fun goToTransferActivity() {
        val intentToList = Intent(this, ChooseTransferList::class.java)
        intentToList.putExtra("start", startLocation)
        intentToList.putExtra("destination", endLocation)
        startActivityForResult(intentToList, 5)
    }

    private fun reviseMarkerInfo(data: MemoData,marker:Marker) {
        realm?.executeTransaction {
            data.title = bottom_slide.titleText.text.toString()
            data.contents = bottom_slide.contentText.text.toString()
            data.month = memo.month
            data.day = memo.day
            data.hour = memo.hour
            data.minute = memo.minute
            data.year = memo.year
        }
        marker.captionText=data.title
        Toast.makeText(this, "메모 내용이 수정되었습니다", Toast.LENGTH_SHORT).show()
    }


}




