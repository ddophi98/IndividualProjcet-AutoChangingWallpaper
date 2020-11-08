package kr.co.ddophi.autochangingwallpaper

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Album (
    var albumImages : MutableList<Uri>,
    var albumTitle : String,
    var pictureCount : Int,
    var representImage : Uri
)




