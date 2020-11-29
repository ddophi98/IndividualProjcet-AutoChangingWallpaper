package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.app.*
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kr.co.ddophi.autochangingwallpaper.R
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
            .setContentTitle("배경화면 변경 진행중")
            .setSmallIcon(R.mipmap.ic_app_show)
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
        return START_STICKY
    }

    //서비스 종료
    override fun onDestroy() {
        Log.d("로그", "서비스 종료")
        isRunning = false
        super.onDestroy()
    }

    //알림 채널 만들기
    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "자동 배경화면 바꾸기 알림", NotificationManager.IMPORTANCE_MIN)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }

    //계속해서 배경화면 바꿔주기 (홈화면)
    private fun runBackground1(intent: Intent) {
        Log.d("로그", "1서비스")

        val albumImages : MutableList<Uri> = loadData(intent)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            changingWallpaper(intent, albumImages, wallpaperManager, delayTime, WallpaperManager.FLAG_SYSTEM)
        }
    }

    //계속해서 배경화면 바꿔주기 (잠금화면)
    private fun runBackground2(intent: Intent) {
        Log.d("로그", "2서비스")


        val albumImages : MutableList<Uri> = loadData(intent)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            changingWallpaper(intent, albumImages, wallpaperManager, delayTime, WallpaperManager.FLAG_LOCK)
        }
    }

    //세팅값에 따라 사진을 순서대로 바꿀지, 랜덤으로 바꿀지 결정
    private fun changingWallpaper (intent: Intent, albumImagesUri : MutableList<Uri>, wallpaperManager: WallpaperManager, delayTime : Long, flag : Int) {

        val albumImagesBitmap = mutableListOf<Bitmap>()
        val phoneHeight = intent.getIntExtra("PhoneHeight", 0)
        val phoneWidth = intent.getIntExtra("PhoneWidth", 0)
        var idx = 0
        val order: String
        val resize: String
        if (flag == WallpaperManager.FLAG_SYSTEM) {
            order = settingValue.homeImageOrder
            resize = settingValue.homeImageResize
        } else {
            order = settingValue.lockImageOrder
            resize = settingValue.lockImageResize
        }

        GlobalScope.launch(Dispatchers.Default) {
            //첫 이미지만 미리 비트맵 변환
            var firstImage = uriToBitmap(albumImagesUri[0])
            if (resize == "fit") {
                firstImage = resizeBitmap(firstImage, phoneWidth, phoneHeight)
            }
            albumImagesBitmap.add(firstImage)
            var bitmapNum = 1

            //다음 이미지부터 코루틴에서 따로 변환
            launch{
                for (uri in albumImagesUri) {
                    if(!isRunning)
                        break
                    if(uri == albumImagesUri[0])
                        continue
                    var newImage = uriToBitmap(uri)
                    if (resize == "fit") {
                        newImage = resizeBitmap(newImage, phoneWidth, phoneHeight)
                    }
                    albumImagesBitmap.add(newImage)
                    bitmapNum++
                }
            }

            //코루틴에서 배경화면 계속 바꿔주기
            launch {
                val random = Random()
                var preIdx: Int
                while (isRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(albumImagesBitmap[idx], null, true, flag)
                    }
                    delay(delayTime)
                    if (order == "inOrder") {
                        idx += 1
                        if (idx == albumImagesUri.size) {
                            idx = 0
                        }
                    } else {
                        preIdx = idx
                        idx = random.nextInt(bitmapNum)
                        while (idx == preIdx) {
                            idx = random.nextInt(bitmapNum)
                        }
                    }
                }
            }
        }
    }

    //세팅 값 받아오기
    private fun loadSetting(intent: Intent) {

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

        settingValue = SettingValue(homeScreen, lockScreen, homeTimeValue, homeTimeType, homeImageResize, homeImageOrder, lockTimeValue, lockTimeType, lockImageResize, lockImageOrder)
        Log.d("로그", "받아온 서비스의 세팅값: $settingValue")
    }

    //따로따로 전달된 사진들을 하나의 리스트에 담기
    private fun loadData(intent: Intent) : MutableList<Uri> {
        val albumImages = mutableListOf<Uri>()
        val size = intent.getIntExtra("Size", 0)
        var uri : Uri
        for(i in 0 until size){
            uri = intent.getParcelableExtra("Picture${i}")!!
            albumImages.add(uri)
        }
        return albumImages
    }


    //Uri 를 Bitmap 으로 변경
    private fun uriToBitmap(uri: Uri) : Bitmap {
        val bitmap : Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val decode = ImageDecoder.createSource(this.contentResolver, uri)
            bitmap = ImageDecoder.decodeBitmap(decode)
        } else {
            Log.d("ProjectLog", "Picture Uri : $uri")
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            Log.d("ProjectLog", "Converted")
        }
        return bitmap
    }

    //기존 비트맵(fill)을 잘리지 않도록(fit)_바꿈
    private fun resizeBitmap(bitmap : Bitmap, width : Int, height : Int) : Bitmap {
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val originalWidth : Float = bitmap.width.toFloat()
        val originalHeight : Float = bitmap.height.toFloat()

        val canvas = Canvas(background)

        val scale1 : Float = width/originalWidth
        val scale2 : Float = height/originalHeight
        val xTranslation : Float
        val yTranslation : Float
        val transformation = Matrix()

        if(scale1 < scale2){
            xTranslation = 0.0f
            yTranslation = (height - originalHeight * scale1) / 2.0f
            transformation.postTranslate(xTranslation, yTranslation)
            transformation.preScale(scale1, scale1)
        }else{
            xTranslation = (width - originalWidth * scale2) / 2.0f
            yTranslation = 0.0f
            transformation.postTranslate(xTranslation, yTranslation)
            transformation.preScale(scale2, scale2)
        }


        val paint = Paint()
        paint.isFilterBitmap = true

        canvas.drawBitmap(bitmap, transformation, paint)

        return background
    }
}