package kr.co.ddophi.autochangingwallpaper.MainActivity

//커스텀 인터페이스
interface MyRecyclerViewInterface {
    fun DeleteButtonClicked(position: Int)
    fun EditButtonClicked(position: Int)
    fun SelectButtonClicked(position: Int)
}