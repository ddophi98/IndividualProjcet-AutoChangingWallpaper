package kr.co.ddophi.autochangingwallpaper

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_recycler.*
import java.io.File
import java.text.ParsePosition

class MainActivity : AppCompatActivity(), MyRecyclerViewInterface {

    private val FLAG_OPEN_GALLERY = 102
    private val FLAG_STORAGE = 99
    private val STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    private lateinit var albumData:MutableList<Album>
    private lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()

        connectAdapter()

        btnAddAlbum.setOnClickListener {
            if(isStoragePermitted()) {
                openGallery()
            }
            Toast.makeText(this, "현재 앨범 개수는 ${albumData.size} 입니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 어답터 연결
    fun connectAdapter () {
        adapter = CustomAdapter(this)
        adapter.albumData = albumData
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // n번째 아이템  클릭
    override fun ItemClicked(position: Int) {
        Toast.makeText(this, "${position+1}번째 아이템입니다", Toast.LENGTH_SHORT).show()
    }

    // n번째 아이템의 삭제 버튼 클릭
    override fun DeleteButtonClicked(position: Int) {
        Toast.makeText(this, "${position+1}번째 앨범 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        albumData.removeAt(position)
        adapter.notifyItemRemoved(position)

        val serviceIntent = Intent(this, AutoChangingService::class.java)
        stopService(serviceIntent)
    }

    // n번째 아이템의 편집 버튼 클릭 (미구현)
    override fun EditButtonClicked(position: Int) {
        Toast.makeText(this, "${position+1}번째 앨범 편집 버튼 클릭", Toast.LENGTH_SHORT).show()
    }

    // n번째 아이템의 선택 버튼 클릭
    override fun SelectButtonClicked(position: Int) {
        Toast.makeText(this, "${position+1}번째 앨범 선택 버튼 클릭", Toast.LENGTH_SHORT).show()

        val serviceIntent = Intent(this, AutoChangingService::class.java)
        for(i in 0 until albumData[position].pictureCount) {
            serviceIntent.putExtra("Picture${i}", albumData[position].albumImages[i])
        }
        serviceIntent.putExtra("Size", albumData[position].pictureCount)

        ContextCompat.startForegroundService(this, serviceIntent)
    }

    // 저장소 권한 있는지 확인하고 없으면 요청
    fun isStoragePermitted(): Boolean {
        if(ContextCompat.checkSelfPermission(this, STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(STORAGE_PERMISSION), FLAG_STORAGE)
            return false
        }
        return true
    }

    // 권한 승인 안되면 메시지 띄우기, 되면 갤러리 열기
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            FLAG_STORAGE -> {
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "저장소 권한을 승인해야지만 앱을 사용할 수 있습니다.", Toast.LENGTH_LONG).show()
                    return
                }else{
                    openGallery()
                }
            }
        }
    }

    //갤러리에서 사진 선택
    fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, FLAG_OPEN_GALLERY)
    }

    //갤러리에서 사진 선택 후 새로운 앨범 생성해서 albumData에 넣기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode){
                FLAG_OPEN_GALLERY -> {
                    val albumImages = mutableListOf<Uri>()
                    val clipData: ClipData? = data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            albumImages.add(clipData.getItemAt(i).uri)
                        }
                    } else {
                        val imageUri = data?.data
                        if (imageUri != null) {
                            albumImages.add(imageUri)
                        }
                    }
                    val pictureCount = if(clipData != null) clipData.itemCount else 1
                    val newAlbum = Album(albumImages, "Album ${albumData.count()+1}", pictureCount)
                    albumData.add(newAlbum)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    fun saveData() {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder().create()
        val json = gson.toJson(albumData)
        editor.putString("Album data", json)
        editor.apply()
    }

    fun loadData() {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val json = sharedPreferences.getString("Album data", null)
        var test = mutableListOf<Album>()
        if(json != null) {
            Log.d("log", "data load 1")
            val type = object : TypeToken<MutableList<Album>>() {}.type
            Log.d("log", "data load 2")
            albumData = gson.fromJson<MutableList<Album>>(json, type)
            Log.d("log", "data load 3")
        }else{
            albumData =  mutableListOf()
        }
    }

    override fun onDestroy() {
        saveData()
        super.onDestroy()
    }
}
