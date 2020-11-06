package kr.co.ddophi.autochangingwallpaper

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MyRecyclerViewInterface {

    private val FLAG_OPEN_GALLERY = 102
    private val FLAG_STORAGE = 99
    private val STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    private val SERVIE_NOT_RUNNING = -1
    private  var currentServicePosition = SERVIE_NOT_RUNNING
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
        }

        btnStop.setOnClickListener {
            if(currentServicePosition == SERVIE_NOT_RUNNING){
                Toast.makeText(this, "현재 설정된 앨범이 없습니다.", Toast.LENGTH_SHORT).show()
            }else{
                val serviceIntent = Intent(this, AutoChangingService::class.java)
                stopService(serviceIntent)
                currentServicePosition = SERVIE_NOT_RUNNING
                Toast.makeText(this, "자동 배경화면 바꾸기가 종료되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 어답터 연결
    fun connectAdapter () {
        adapter = CustomAdapter(this)
        adapter.albumData = albumData
        albumRecyclerView.adapter = adapter
        albumRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // n번째 아이템  클릭
    override fun ItemClicked(position: Int) {
        Toast.makeText(this, "${position+1}번째 아이템입니다", Toast.LENGTH_SHORT).show()
    }

    // n번째 아이템의 삭제 버튼 클릭
    override fun DeleteButtonClicked(position: Int) {
        when {
            currentServicePosition == position -> {
                showDeletePopup2(position)
            }
            currentServicePosition > position -> {
                currentServicePosition -= 1
                showDeletePopup1(position)
            }
            else -> {
                showDeletePopup1(position)
            }
        }
    }

    // n번째 아이템의 편집 버튼 클릭 (미구현)
    override fun EditButtonClicked(position: Int) {
        Toast.makeText(this, "${position+1}번째 앨범 편집 버튼 클릭", Toast.LENGTH_SHORT).show()

        val editAlbumIntent = Intent(this, EditAlbumActivity::class.java)

        for (i in 0 until albumData[position].pictureCount) {
            editAlbumIntent.putExtra("Picture${i}", albumData[position].albumImages[i])
        }
        editAlbumIntent.putExtra("Size", albumData[position].pictureCount)
        editAlbumIntent.putExtra("Title", albumData[position].albumTitle)

        startActivity(editAlbumIntent)

    }

    // n번째 아이템의 선택 버튼 클릭
    override fun SelectButtonClicked(position: Int) {
        Toast.makeText(this, "${albumData[position].albumTitle} 앨범이 선택되었습니다. 배경화면이 자동으로 바뀝니다.", Toast.LENGTH_LONG).show()

        if(position != currentServicePosition) {
            val serviceIntent = Intent(this, AutoChangingService::class.java)
            if(currentServicePosition != SERVIE_NOT_RUNNING) {
                stopService(serviceIntent)
            }
            for (i in 0 until albumData[position].pictureCount) {
                serviceIntent.putExtra("Picture${i}", albumData[position].albumImages[i])
            }
            serviceIntent.putExtra("Size", albumData[position].pictureCount)

            ContextCompat.startForegroundService(this, serviceIntent)
            currentServicePosition = position
        }
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
                    saveData()
                }
            }
        }
    }

    //미완성
    fun saveData() {
/*        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder().create()
        val json = gson.toJson(albumData)
        editor.putString("Album data", json)
        editor.apply() */
    }

    //미완성
    fun loadData() {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val json = sharedPreferences.getString("Album data", null)
        var test = mutableListOf<Album>()
        if(json != null) {
            Log.d("로그", "data load 1")
            val type = object : TypeToken<MutableList<Album>>() {}.type
            Log.d("로그", "data load 2")
            albumData = gson.fromJson<MutableList<Album>>(json, type)
            Log.d("로그", "data load 3")
        }else{
            albumData =  mutableListOf()
        }
    }

    //앨범을 삭제할 때 나오는 팝업 메시지
    fun showDeletePopup1(position: Int) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("앨범 삭제")
            .setMessage("앨범을 정말 삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                albumData.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this, "앨범이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

            }
            .setNeutralButton("취소", null)
            .create()

        alertDialog.show()
    }

    //서비스가 실행중인 앨범을 삭제할 때 나오는 팝업 메시지
    fun showDeletePopup2(position: Int) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("앨범 삭제")
            .setMessage("현재 배경화면으로 지정된 앨범입니다. 삭제하시면 다른 앨범을 다시 선택해야 합니다. 삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                val serviceIntent = Intent(this, AutoChangingService::class.java)
                stopService(serviceIntent)
                currentServicePosition = SERVIE_NOT_RUNNING
                albumData.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this, "앨범이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("취소", null)
            .create()

        alertDialog.show()
    }

    //미완성
    override fun onDestroy() {
        saveData()
        Log.d("로그", "종료")
        super.onDestroy()
    }
}
