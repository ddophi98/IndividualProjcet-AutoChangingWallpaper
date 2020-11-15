package kr.co.ddophi.autochangingwallpaper.EditActivity

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_picture_show.*
import kr.co.ddophi.autochangingwallpaper.R

//사진 전체화면에 보여주는 액티비티
class PictureShowActivity : AppCompatActivity() {

    var actionBarShow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_show)

        //클릭한 사진 uri 가져와서 보여주기(확대 가능)
        val uri = intent.getParcelableExtra<Uri>("Uri")
        enlargedPicture.setImageURI(uri)

        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //클릭하면 액션바 없애고 보여주기
        enlargedPicture.setOnClickListener {
            if(!actionBarShow){
                actionBarShow =true
                supportActionBar?.show()
            }else{
                actionBarShow = false
                supportActionBar?.hide()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

