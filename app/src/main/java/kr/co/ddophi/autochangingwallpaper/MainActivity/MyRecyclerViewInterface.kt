package kr.co.ddophi.autochangingwallpaper.MainActivity

//커스텀 인터페이스
interface MyRecyclerViewInterface {
    fun deleteButtonClicked(position: Int)
    fun editButtonClicked(position: Int)
    fun selectButtonClicked(position: Int)
    fun albumTitleClicked(position: Int, title: String)
    fun representImageClicked(position: Int)
}