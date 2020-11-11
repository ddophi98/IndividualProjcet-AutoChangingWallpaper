package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.ddophi.autochangingwallpaper.R


class AutoChangingService : Service() {

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
            runBackground(intent)
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

    //계속해서 배경화면 바꿔주기
    fun runBackground(intent: Intent) {
        val albumImages = loadData(intent)
        val wallpaperManager : WallpaperManager = WallpaperManager.getInstance(this)

        var idx = 0
        GlobalScope.launch(Dispatchers.Default) {
            while(isRunning){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    wallpaperManager.setBitmap(albumImages[idx])
                }
                idx++
                delay(2000)
                if(idx == albumImages.size) {
                    idx = 0
                }
            }
        }
    }

    //따로따로 전달된 사진들을 하나의  Bitmap 리스트에 담기
    fun loadData(intent: Intent) : MutableList<Bitmap> {
        val albumImages = mutableListOf<Bitmap>()
        val size = intent.getIntExtra("Size", 0)
        var uri : Uri
        for(i in 0 until size){
            uri = intent.getParcelableExtra<Uri>("Picture${i}")!!
            albumImages.add(uriToBitmap(uri))
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
}
