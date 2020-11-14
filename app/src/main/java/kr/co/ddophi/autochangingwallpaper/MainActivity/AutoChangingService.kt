package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import kr.co.ddophi.autochangingwallpaper.R
import kr.co.ddophi.autochangingwallpaper.SettingActivity
import kr.co.ddophi.autochangingwallpaper.SettingValue
import java.util.*


class AutoChangingService : Service() {

    lateinit var settingValue : SettingValue
    val CHANNEL_ID = "ForegroundChannel"
    val NOTI_ID = 123
    var isRunning = false

    // 사용 안함
    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    //Foreground 서비스 돌아가게 하기
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("로그", "서비스 시작")

        createNotificationChannel()
        val notification : Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()

        startForeground(NOTI_ID, notification)

        isRunning = true
        if(intent != null) {
            loadSetting(intent)

            //세팅값에 따라 홈화면만 실행할지, 잠금화면만 실행할지, 둘 다 실행할지 결정
            if(settingValue.homeScreen) {
                runBackground1(intent)
            }
            if(settingValue.lockScreen) {
                runBackground2(intent)
            }
        }
        return Service.START_STICKY
    }

    //서비스 종료
    override fun onDestroy() {
        Log.d("로그", "서비스 종료")
        isRunning = false
        super.onDestroy()
    }

    //알림 채널 만들기
    fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_MIN)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }

    //계속해서 배경화면 바꿔주기 (홈화면)
    fun runBackground1(intent: Intent) {
        Log.d("로그", "1서비스")
        val albumImages : MutableList<Bitmap>

        //세팅값에 따라 이미지를 fill 로 할지, fit 으로 할지 결정
        if(settingValue.homeImageResize == "fill"){
            albumImages = loadDataFill(intent)
        }else{
            albumImages = loadDataFit(intent)
        }

        val wallpaperManager : WallpaperManager = WallpaperManager.getInstance(this)

        //세팅값에 따라 바뀌는 시간 주기를 얼마나 할지 결정
        val delayTime : Long = when(settingValue.homeTimeType){
            "seconds" -> {
                settingValue.homeTimeValue.toLong() * 1000
            }
            "minutes" -> {
                settingValue.homeTimeValue.toLong() * 1000 * 60
            }
            "hours" -> {
                settingValue.homeTimeValue.toLong() * 1000 * 60 * 60
            }
            else -> {
                settingValue.homeTimeValue.toLong() * 1000 * 60 * 60 * 24
            }
        }

        val idx = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            changingWallpaper(albumImages, wallpaperManager, delayTime, idx, WallpaperManager.FLAG_SYSTEM)
        }
    }

    //계속해서 배경화면 바꿔주기 (잠금화면)
    fun runBackground2(intent: Intent) {
        Log.d("로그", "2서비스")
        val albumImages : MutableList<Bitmap>

        //세팅값에 따라 이미지를 fill 로 할지, fit 으로 할지 결정
        if(settingValue.lockImageResize == "fill"){
            albumImages = loadDataFill(intent)
        }else{
            albumImages = loadDataFit(intent)
        }

        val wallpaperManager : WallpaperManager = WallpaperManager.getInstance(this)

        //세팅값에 따라 바뀌는 시간 주기를 얼마나 할지 결정
        val delayTime : Long = when(settingValue.lockTimeType){
            "seconds" -> {
                settingValue.lockTimeValue.toLong() * 1000
            }
            "minutes" -> {
                settingValue.lockTimeValue.toLong() * 1000 * 60
            }
            "hours" -> {
                settingValue.lockTimeValue.toLong() * 1000 * 60 * 60
            }
            else -> {
                settingValue.lockTimeValue.toLong() * 1000 * 60 * 60 * 24
            }
        }

        val idx = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            changingWallpaper(albumImages, wallpaperManager, delayTime, idx, WallpaperManager.FLAG_LOCK)
        }
    }

    //세팅값에 따라 사진을 순서대로 바꿀지, 랜덤으로 바꿀지 결정
    fun changingWallpaper (albumImages : MutableList<Bitmap>, wallpaperManager: WallpaperManager, delayTime : Long, index : Int, flag : Int) {
        var idx = index
        if(settingValue.lockImageOrder == "inOrder") {
            GlobalScope.launch(Dispatchers.Default) {
                while (isRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(albumImages[idx], null, true, flag)
                    }
                    delay(delayTime)
                    idx += 1
                    if (idx == albumImages.size) {
                        idx = 0
                    }

                }
            }
        }else{
            GlobalScope.launch(Dispatchers.Default) {
                var preIdx = idx
                val random = Random()
                while (isRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(albumImages[idx], null, true, flag)
                    }
                    delay(delayTime)
                    preIdx = idx
                    idx = random.nextInt(albumImages.size)
                    while(idx == preIdx){
                        idx = random.nextInt(albumImages.size)
                    }
                }
            }
        }
    }

    //세팅 값 받아오기
    fun loadSetting(intent: Intent) {

        val homeScreen = intent.getBooleanExtra("HomeScreen", false)
        val lockScreen = intent.getBooleanExtra("LockScreen", false)
        val homeTimeValue = intent.getStringExtra("TimeValue_Home")!!
        val homeTimeType = intent.getStringExtra("TimeType_Home")!!
        val homeImageResize = intent.getStringExtra("ImageResize_Home")!!
        val homeImageOrder = intent.getStringExtra("ImageOrder_Home")!!
        val lockTimeValue = intent.getStringExtra("TimeValue_Lock")!!
        val lockTimeType = intent.getStringExtra("TimeType_Lock")!!
        val lockImageResize = intent.getStringExtra("ImageResize_Lock")!!
        val lockImageOrder = intent.getStringExtra("ImageOrder_Lock")!!
        val doubleTap = intent.getBooleanExtra("DoubleTap", false)

        settingValue = SettingValue(homeScreen, lockScreen, homeTimeValue, homeTimeType, homeImageResize, homeImageOrder, lockTimeValue, lockTimeType, lockImageResize, lockImageOrder, doubleTap)
        Log.d("로그", "받아온 서비스의 세팅값: ${settingValue}")
    }

    //따로따로 전달된 사진들을 하나의  Bitmap 리스트에 담기(fill)
    fun loadDataFill(intent: Intent) : MutableList<Bitmap> {

        val phoneHeight = intent.getIntExtra("PhoneHeight", 0)
        val phoneWidth = intent.getIntExtra("PhoneWidth", 0)

        val albumImages = mutableListOf<Bitmap>()
        val size = intent.getIntExtra("Size", 0)
        var uri : Uri
        for(i in 0 until size){
            uri = intent.getParcelableExtra<Uri>("Picture${i}")!!
            albumImages.add(uriToBitmap(uri))
        }
        return albumImages
    }

    //따로따로 전달된 사진들을 하나의  Bitmap 리스트에 담기(fit)
    fun loadDataFit(intent: Intent) : MutableList<Bitmap> {

        val phoneHeight = intent.getIntExtra("PhoneHeight", 0)
        val phoneWidth = intent.getIntExtra("PhoneWidth", 0)

        val albumImages = mutableListOf<Bitmap>()
        val size = intent.getIntExtra("Size", 0)
        var uri : Uri
        for(i in 0 until size){
            uri = intent.getParcelableExtra<Uri>("Picture${i}")!!
            albumImages.add(resizeBitmap(uriToBitmap(uri), phoneWidth, phoneHeight))
        }
        return albumImages
    }

    //Uri 를 Bitmap 으로 변경
    fun uriToBitmap(uri: Uri) : Bitmap {
        val bitmap : Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val decode = ImageDecoder.createSource(this.contentResolver, uri)
            bitmap = ImageDecoder.decodeBitmap(decode)
        } else {
            Log.d("ProjectLog", "Picture Uri : ${uri}")
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            Log.d("ProjectLog", "Converted")
        }
        return bitmap
    }

    //기존 비트맵(fill)을 잘리지 않도록(fit)_바꿈
    fun resizeBitmap(bitmap : Bitmap, width : Int, height : Int) : Bitmap {
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val originalWidth : Float = bitmap.width.toFloat()
        val originalHeight : Float = bitmap.height.toFloat()

        val canvas = Canvas(background)

        val scale : Float = width/originalWidth

        val xTranslation = 0.0f
        val yTranslation = (height - originalHeight * scale) / 2.0f

        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)

        val paint = Paint()
        paint.isFilterBitmap = true

        canvas.drawBitmap(bitmap, transformation, paint)

        return background
    }
}

//미구현
abstract class DoubleClickListener : View.OnClickListener {
    val DEFAULT_QUALIFICATION_SPAN : Long = 200
    var  doubleClickQualificationSpanInMillis : Long? = null
    var timestampLastClick : Long? = null

    fun DoubleClickListener() {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN
        timestampLastClick = 0
    }

    override fun onClick(v: View?) {
        if((SystemClock.elapsedRealtime() - timestampLastClick!!) < doubleClickQualificationSpanInMillis!!) {
            onDoubleClick()
        }
        timestampLastClick = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick()
}
