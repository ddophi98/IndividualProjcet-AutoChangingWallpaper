package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        //전원 꺼졌다가 다시 켜졌을 때 서비스 다시 실행 (미완성 : intent 값을 넣을 수 없음)
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("test1", "리부팅 후 서비스 재실행")
            val serviceIntent = Intent(context, AutoChangingService::class.java)
            ContextCompat.startForegroundService(context!!, serviceIntent)
        }
    }
}