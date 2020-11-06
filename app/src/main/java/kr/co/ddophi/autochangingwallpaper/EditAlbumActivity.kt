package kr.co.ddophi.autochangingwallpaper

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_edit_album.*

//앨범 편집 화면 (사진 여러개 나열한 리사이클러뷰 이용)
class EditAlbumActivity : AppCompatActivity() {

    val album = mutableListOf<Uri>()
    var albumTitle : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_album)

        loadData()

        val adapter = EditAlbumAdapter()
        adapter.album = album
        pictureRecyclerView.adapter = adapter
        pictureRecyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    //액션바 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        supportActionBar?.title = albumTitle.toString()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return true
    }

    //액션바의 뒤로가기 버튼 설정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                Toast.makeText(this, "뒤로가기", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //main 함수에서 넘어온 사진 데이터 받음
    fun loadData() {
        albumTitle = intent.getStringExtra("Title")
        val size = intent.getIntExtra("Size", 0)
        var pictureUri : Uri
        for(i in 0 until size){
            pictureUri = intent.getParcelableExtra<Uri>("Picture${i}")!!
            album.add(pictureUri)
        }
    }
}
