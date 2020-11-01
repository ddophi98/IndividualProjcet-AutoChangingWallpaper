package kr.co.ddophi.autochangingwallpaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val albumData:MutableList<Album> = loadData()
        var adapter = CustomAdapter()
        adapter.albumData = albumData
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

 //       val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager(this).orientation)
 //       recyclerView.addItemDecoration((dividerItemDecoration))

    }

    // 가상 데이터 만들기 (나중에 바꿀거)
    fun loadData(): MutableList<Album> {
        val albumData:MutableList<Album> = mutableListOf()
        for(no in 1..100){
            val albumTitle = "앨범 ${no}"
            val pictureCount = no * 2
            var album = Album(albumTitle, pictureCount)
            albumData.add(album)
        }
        return albumData
    }
}
