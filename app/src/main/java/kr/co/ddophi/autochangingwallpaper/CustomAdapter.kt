package kr.co.ddophi.autochangingwallpaper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler.view.*

class CustomAdapter : RecyclerView.Adapter<Holder>() {

    var albumData = mutableListOf<Album>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return albumData.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val album = albumData.get(position)
        holder.setAlbum(album)

    }

}

class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun setAlbum(album : Album) {
        itemView.albumTitle.text = album.albumTitle
        itemView.pictureCount.text = "${album.pictureCount}"
        itemView.albumImage.setImageURI(album.albumImages[0])
    }
}