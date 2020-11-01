package kr.co.ddophi.autochangingwallpaper

import android.net.Uri

data class Album (
    var albumImages : MutableList<Uri>,
    var albumTitle : String,
    var pictureCount : Int
)



