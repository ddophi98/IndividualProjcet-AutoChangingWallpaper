package kr.co.ddophi.autochangingwallpaper

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val FLAG_OPEN_GALLERY = 102
    private val FLAG_STORAGE = 99
    private val STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    private val albumData:MutableList<Album> = mutableListOf()
    private lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectAdapter()

        btnAddAlbum.setOnClickListener {
            if(isStoragePermitted()) {
                openGallery()
            }
        }
    }

    // 어답터 연결
    fun connectAdapter () {
        adapter = CustomAdapter()
        adapter.albumData = albumData
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
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



}
