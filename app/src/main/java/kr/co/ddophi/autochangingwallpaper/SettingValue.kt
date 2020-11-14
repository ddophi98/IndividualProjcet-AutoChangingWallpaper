package kr.co.ddophi.autochangingwallpaper

data class SettingValue (
    var homeScreen : Boolean,
    var lockScreen  : Boolean,
    var homeTimeValue : String,
    var homeTimeType : String,
    var homeImageResize : String,
    var homeImageOrder : String,
    var lockTimeValue : String,
    var lockTimeType : String,
    var lockImageResize : String,
    var lockImageOrder : String,
    var doubleTap  : Boolean
)