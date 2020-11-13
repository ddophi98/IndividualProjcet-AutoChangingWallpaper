package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_recycler.*
import kr.co.ddophi.autochangingwallpaper.*
import kr.co.ddophi.autochangingwallpaper.EditActivity.EditAlbumActivity


class MainActivity : AppCompatActivity(), MyRecyclerViewInterface {

    private val FLAG_SETTING_ACTIVITY = 102
    private val FLAG_OPEN_GALLERY = 101
    private val FLAG_EDIT_ALBUM_ACTIVITY = 100
    private val FLAG_STORAGE = 99
    private val STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE

    private val SERVIE_NOT_RUNNING = -1
    private val EDIT_NOT_PROCESSING = -1
    private var currentEditPosition = EDIT_NOT_PROCESSING
    private  var currentServicePosition = SERVIE_NOT_RUNNING

    private var SETTING_HOME_SCREEN = false
    private var SETTING_LOCK_SCREEN = false

    private lateinit var albumData:MutableList<Album>
    private lateinit var adapter: CustomAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //기존 데이터와 설정값 가져오기
        loadData()
        loadSetting()

        //리사이클러뷰 어답터 연결
        connectAdapter()

        //추가 버튼 동작
        btnAddAlbum.setOnClickListener {
            if(isStoragePermitted()) {
                openGallery()
            }
        }

        //서비스 종료 버튼 동작
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
        adapter = CustomAdapter(this, this)
        adapter.albumData = albumData
        albumRecyclerView.adapter = adapter
        albumRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    //액션바 메뉴 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    //액션바 메뉴 눌렀을 때 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.btnSetting -> {
                val settingIntent = Intent(this, SettingActivity::class.java)
                startActivityForResult(settingIntent, FLAG_SETTING_ACTIVITY)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // n번째 아이템의 제목 설정 클릭 및 작성
    override fun albumTitleClicked(position: Int, title: String) {
        albumData[position].albumTitle = title
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

    // n번째 아이템의 편집 버튼 클릭
    override fun EditButtonClicked(position: Int) {
        currentEditPosition = position
        val editAlbumIntent = Intent(this, EditAlbumActivity::class.java)

        for (i in 0 until albumData[position].pictureCount) {
            editAlbumIntent.putExtra("Picture${i}", albumData[position].albumImages[i])
        }
        editAlbumIntent.putExtra("Size", albumData[position].pictureCount)
        editAlbumIntent.putExtra("Title", albumData[position].albumTitle)
        editAlbumIntent.putExtra("Represent", albumData[position].representImage)

        startActivityForResult(editAlbumIntent, FLAG_EDIT_ALBUM_ACTIVITY)
    }

    // n번째 아이템의 선택 버튼 클릭
    override fun SelectButtonClicked(position: Int) {
        if(!SETTING_HOME_SCREEN && !SETTING_LOCK_SCREEN) {
            Toast.makeText(this, "설정창에서 적어도 한개의 화면은 선택해주세요", Toast.LENGTH_LONG).show()
        }else {
            if (currentServicePosition != position) {
                Toast.makeText(
                    this,
                    "${albumData[position].albumTitle} 앨범이 선택되었습니다. 배경화면이 자동으로 바뀝니다.",
                    Toast.LENGTH_LONG
                ).show()

                val serviceIntent = Intent(this, AutoChangingService::class.java)

                if (currentServicePosition != SERVIE_NOT_RUNNING) {
                    stopService(serviceIntent)
                }

                for (i in 0 until albumData[position].pictureCount) {
                    serviceIntent.putExtra("Picture${i}", albumData[position].albumImages[i])
                }
                serviceIntent.putExtra("Size", albumData[position].pictureCount)
                serviceIntent.putExtra("HomeScreen", SETTING_HOME_SCREEN)
                serviceIntent.putExtra("LockScreen", SETTING_LOCK_SCREEN)

                ContextCompat.startForegroundService(this, serviceIntent)
                currentServicePosition = position
            } else {
                Toast.makeText(this, "이미 선택된 앨범입니다.", Toast.LENGTH_SHORT).show()
            }
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

    //갤러리에서 사진 선택 (다중 선택 버전 고려)
    fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, FLAG_OPEN_GALLERY)
    }

    //다른 액티비티 결과 받아오기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                //갤러리에서 사진 선택 후 새로운 앨범 생성해서 albumData 리스트에 넣기
                 FLAG_OPEN_GALLERY -> {
                     val albumImages = mutableListOf<Uri>()
                     var pictureCount = 0

                     val takeflags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                             Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                     val clipData: ClipData? = data?.clipData
                     if (clipData != null) {
                         pictureCount = clipData.itemCount
                         for (i in 0 until pictureCount) {
                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                 contentResolver.takePersistableUriPermission(clipData.getItemAt(i).uri, takeflags)
                             }
                             albumImages.add(clipData.getItemAt(i).uri)
                         }
                     } else {
                         pictureCount = 1
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                             contentResolver.takePersistableUriPermission(data?.data!!, takeflags)
                         }
                         albumImages.add(data?.data!!)
                     }

                     val newAlbum = Album(albumImages, "", pictureCount, albumImages[0])
                     albumData.add(newAlbum)

                     adapter.notifyDataSetChanged()
                 }
                // 편집한 앨범에서 편집된 것들 업데이트
                FLAG_EDIT_ALBUM_ACTIVITY -> {
                    val currentAlbum = albumData[currentEditPosition]
                    currentAlbum.albumImages.clear()

                    currentAlbum.pictureCount = data!!.getIntExtra("Size", -1)
                    currentAlbum.representImage = data.getParcelableExtra<Uri>("Represent")!!
                    var pictureUri: Uri
                    for (i in 0 until currentAlbum.pictureCount) {
                        pictureUri = data.getParcelableExtra<Uri>("Picture${i}")!!
                        currentAlbum.albumImages.add(pictureUri)
                    }

                    adapter.notifyDataSetChanged()
                    currentEditPosition = EDIT_NOT_PROCESSING
                }
                //세팅값 업데이트
                FLAG_SETTING_ACTIVITY -> {
                    loadSetting()
                    if(currentServicePosition != SERVIE_NOT_RUNNING){
                        val serviceIntent = Intent(this, AutoChangingService::class.java)

                        //서비스 멈춤
                        stopService(serviceIntent)
                        val position = currentServicePosition
                        currentServicePosition = SERVIE_NOT_RUNNING

                        //서비스 다시 시작
                        if(!(!SETTING_HOME_SCREEN && !SETTING_LOCK_SCREEN)) {
                            for (i in 0 until albumData[position].pictureCount) {
                                serviceIntent.putExtra(
                                    "Picture${i}",
                                    albumData[position].albumImages[i]
                                )
                            }
                            serviceIntent.putExtra("Size", albumData[position].pictureCount)
                            serviceIntent.putExtra("HomeScreen", SETTING_HOME_SCREEN)
                            serviceIntent.putExtra("LockScreen", SETTING_LOCK_SCREEN)

                            ContextCompat.startForegroundService(this, serviceIntent)
                            currentServicePosition = position
                        }
                    }
                }
            }
        }
    }

    //서비스가 실행중이지 않은 앨범을 삭제할 때나오는 팝업 메시지
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

    //데이터 저장(albumData)
    fun saveData() {
        val sharedPreferences = getSharedPreferences("SharedPreferences_AlbumData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder().registerTypeHierarchyAdapter(Uri::class.java, UriTypeAdapter()).create()
        val json = gson.toJson(albumData)
        editor.putString("Album data", json)
        editor.putInt("Current Service Position", currentServicePosition)
        editor.apply()
    }

    //데이터 불러오기
    fun loadData() {
        val sharedPreferences = getSharedPreferences("SharedPreferences_AlbumData", Context.MODE_PRIVATE)
        currentServicePosition = sharedPreferences.getInt("Current Service Position", SERVIE_NOT_RUNNING)
        val gson = GsonBuilder().registerTypeHierarchyAdapter(Uri::class.java, UriTypeAdapter()).create()
        val json = sharedPreferences.getString("Album data", null)
        if(json != null) {
            val type = object : TypeToken<MutableList<Album>>() {}.type
            albumData = gson.fromJson<MutableList<Album>>(json, type)
        }else{
            albumData =  mutableListOf()
        }
    }

    //설정 값 불러오기
    fun loadSetting() {
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        SETTING_HOME_SCREEN = defaultSharedPreferences.getBoolean("HomeScreen", false)
        SETTING_LOCK_SCREEN = defaultSharedPreferences.getBoolean("LockScreen", false)
    }

    //앱을 종료하거나 나갈때마다 저장
    override fun onPause() {
        saveData()
        super.onPause()
    }
}


