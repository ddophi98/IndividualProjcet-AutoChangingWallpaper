package kr.co.ddophi.autochangingwallpaper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_welcome.*
import kr.co.ddophi.autochangingwallpaper.MainActivity.MainActivity

class WelcomeActivity : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    lateinit var layouts : ArrayList<Int>
    lateinit var dots : ArrayList<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("SharedPreferences_CheckFirstLaunch", Context.MODE_PRIVATE)

        val firstLaunch = preferences.getBoolean("First Launch", true)
        if(!firstLaunch){
            launchMainActivity()
            finish()
        }

        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }

        setContentView(R.layout.activity_welcome)

        layouts = arrayListOf(R.layout.welcome_slide1, R.layout.welcome_slide2, R.layout.welcome_slide3, R.layout.welcome_slide4)
        dots = arrayListOf(TextView(this), TextView(this), TextView(this), TextView(this))
        addBottomDots(0)
        viewPager.adapter = CustomPagerAdapter()

        btnSkip.setOnClickListener {
            launchMainActivity()
        }

        btnNext.setOnClickListener {
            val current = viewPager.currentItem + 1
            if(current < layouts.size){
                viewPager.currentItem = current
            }else{
                launchMainActivity()
            }
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
               addBottomDots(position)
                if(position == layouts.size - 1) {
                    btnNext.text = "GOT IT"
                    btnSkip.visibility = View.GONE
                }else{
                    btnNext.text = "NEXT"
                    btnSkip.visibility = View.VISIBLE
                }
            }
        })
    }

    fun addBottomDots(currentPage : Int){
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            layoutDots.removeAllViews()
            for (i in 0 until dots.size) {
                dots[i].text = Html.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
                dots[i].textSize = 35f
                dots[i].setTextColor(colorsInactive[currentPage])
                layoutDots.addView(dots[i])
            }

            dots[currentPage].setTextColor(colorsActive[currentPage])
        }
    }

    private fun launchMainActivity() {
        val preferencesEditor = preferences.edit()
        preferencesEditor.putBoolean("First Launch", false)
        preferencesEditor.apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    inner class CustomPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)

            return view
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }
}
