package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kr.co.ddophi.autochangingwallpaper.Album
import kr.co.ddophi.autochangingwallpaper.SettingValue

class BootReceiver : BroadcastReceiver() {
    val SERVIE_NOT_RUNNING = -1
    var currentServicePosition = SERVIE_NOT_RUNNING
    private lateinit var settingValue : SettingValue
    private lateinit var albumData:MutableList<Album>

    override fun onReceive(context: Context?, intent: Intent?) {
        //전원 꺼졌다가 다시 켜졌을 때 서비스 다시 실행
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("로그", "리부팅 후 서비스 재실행")

            if(currentServicePosition != SERVIE_NOT_RUNNING){
                loadData(context!!)
                loadSetting(context)
                val serviceIntent = Intent(context, AutoChangingService::class.java)
                putValue(context, serviceIntent, currentServicePosition)
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }

    private fun putValue(context: Context, serviceIntent: Intent, position: Int){
        for (i in 0 until albumData[position].pictureCount) {
            serviceIntent.putExtra("Picture${i}", albumData[position].albumImages[i])
        }
        serviceIntent.putExtra("Size", albumData[position].pictureCount)

        val metrics = DisplayMetrics()
        context.display?.getRealMetrics(metrics)

        serviceIntent.putExtra("PhoneHeight", metrics.heightPixels)
        serviceIntent.putExtra("PhoneWidth", metrics.widthPixels)

        serviceIntent.putExtra("HomeScreen", settingValue.homeScreen)
        serviceIntent.putExtra("LockScreen", settingValue.lockScreen)
        serviceIntent.putExtra("TimeValue_Home", settingValue.homeTimeValue)
        serviceIntent.putExtra("TimeType_Home", settingValue.homeTimeType)
        serviceIntent.putExtra("ImageResize_Home", settingValue.homeImageResize)
        serviceIntent.putExtra("ImageOrder_Home", settingValue.homeImageOrder)
        serviceIntent.putExtra("TimeValue_Lock", settingValue.lockTimeValue)
        serviceIntent.putExtra("TimeType_Lock", settingValue.lockTimeType)
        serviceIntent.putExtra("ImageResize_Lock", settingValue.lockImageResize)
        serviceIntent.putExtra("ImageOrder_Lock", settingValue.lockImageOrder)
    }

    //데이터 불러오기
    private fun loadData(context: Context) {
        val sharedPreferences = context.getSharedPreferences("SharedPreferences_AlbumData", Context.MODE_PRIVATE)
        currentServicePosition = sharedPreferences.getInt("Current Service Position", SERVIE_NOT_RUNNING)
        val gson = GsonBuilder().registerTypeHierarchyAdapter(Uri::class.java, UriTypeAdapter()).create()
        val json = sharedPreferences.getString("Album data", null)
        albumData = if(json != null) {
            val type = object : TypeToken<MutableList<Album>>() {}.type
            gson.fromJson<MutableList<Album>>(json, type)
        }else{
            mutableListOf()
        }
    }

    //설정 값 불러오기
    private fun loadSetting(context: Context) {
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

        val homeScreen = defaultSharedPreferences.getBoolean("HomeScreen", false)
        val lockScreen = defaultSharedPreferences.getBoolean("LockScreen", false)
        val homeTimeValue = defaultSharedPreferences.getString("TimeValue_Home", "")!!
        val homeTimeType = defaultSharedPreferences.getString("TimeType_Home", "")!!
        val homeImageResize = defaultSharedPreferences.getString("ImageResize_Home", "")!!
        val homeImageOrder = defaultSharedPreferences.getString("ImageOrder_Home", "")!!
        val lockTimeValue = defaultSharedPreferences.getString("TimeValue_Lock", "")!!
        val lockTimeType = defaultSharedPreferences.getString("TimeType_Lock", "")!!
        val lockImageResize = defaultSharedPreferences.getString("ImageResize_Lock", "")!!
        val lockImageOrder = defaultSharedPreferences.getString("ImageOrder_Lock", "")!!

        settingValue = SettingValue(homeScreen, lockScreen, homeTimeValue, homeTimeType, homeImageResize, homeImageOrder, lockTimeValue, lockTimeType, lockImageResize, lockImageOrder)
    }
}