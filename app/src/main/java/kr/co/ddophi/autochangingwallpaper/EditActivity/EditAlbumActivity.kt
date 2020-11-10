package kr.co.ddophi.autochangingwallpaper.EditActivity

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_edit_album.*
import kr.co.ddophi.autochangingwallpaper.R

//앨범 편집 화면
class EditAlbumActivity : AppCompatActivity() {

    private val FLAG_OPEN_GALLERY = 101
    val album = mutableListOf<Uri>()
    var albumTitle : String? = null
    var representImage : Uri? = null
    private lateinit var adapter: EditAlbumAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_album)

        loadData()

        connectAdapter()

        //편집모드에서 사진 추가
        btnEditAdd.setOnClickListener{
            openGallery()
        }

        supportActionBar?.setTitle(albumTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Toast.makeText(this, "사진을 선택하려면 꾹 누르세요\n(짧게 터치하면 사진을 자세히 볼 수 있습니다.)", Toast.LENGTH_LONG).show()
    }

    // 어답터 연결
    fun connectAdapter () {
        adapter = EditAlbumAdapter(this, albumTitle, album, representImage)
        pictureRecyclerView.adapter = adapter
        pictureRecyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    //액션바의 뒤로가기 버튼 클릭
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //핸드폰 자체의 뒤로가기 버튼 클릭, 편집된 내용들 MainActivity 로 보내줌
    override fun onBackPressed() {
        val returnIntent = Intent()
        for (i in 0 until adapter.album.size) {
            returnIntent.putExtra("Picture${i}", adapter.album[i])
        }
        returnIntent.putExtra("Size", adapter.album.size)
        returnIntent.putExtra("Represent", adapter.representImage)
        setResult(Activity.RESULT_OK, returnIntent)
        super.onBackPressed()
    }


    //main 함수에서 넘어온 사진 데이터 받음
    fun loadData() {
        albumTitle = intent.getStringExtra("Title")
        representImage = intent.getParcelableExtra("Represent")
        val size = intent.getIntExtra("Size", 0)
        var pictureUri : Uri
        for(i in 0 until size){
            pictureUri = intent.getParcelableExtra<Uri>("Picture${i}")!!
            album.add(pictureUri)
        }
    }

    //갤러리에서 사진 선택 (다중 선택 버전 고려)
    fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, FLAG_OPEN_GALLERY)
    }

    //갤러리에서 사진 선택 후 기존 앨범에 추가하기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode){
                FLAG_OPEN_GALLERY -> {
                    val clipData: ClipData? = data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            album.add(clipData.getItemAt(i).uri)
                        }
                    } else {
                        val imageUri = data?.data
                        Log.d("로그", "${imageUri}")
                        if (imageUri != null) {
                            album.add(imageUri)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

   /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {

            var isContain = false

            when(requestCode){
                FLAG_OPEN_GALLERY -> {
                    val clipData: ClipData? = data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            if(album.contains(clipData.getItemAt(i).uri)){
                                isContain = true
                                break
                            }
                        }
                        if(!isContain) {
                            for (i in 0 until clipData.itemCount) {
                                album.add(clipData.getItemAt(i).uri)
                                Log.d("로그", "추가1")
                            }
                        }
                    } else {
                        val imageUri = data?.data
                        if(album.contains(imageUri)){
                            isContain = true
                        }
                        if (imageUri != null && !isContain) {
                            album.add(imageUri)
                            Log.d("로그", "추가2")
                        }
                    }
                    adapter.notifyDataSetChanged()
                    if(isContain){
                        Toast.makeText(this, "이미 포함된 사진이 있습니다. 다시 골라주세요", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    } */
}
