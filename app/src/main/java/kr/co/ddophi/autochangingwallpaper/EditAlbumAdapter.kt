package kr.co.ddophi.autochangingwallpaper

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.picture_recycler.view.*

class EditAlbumAdapter : RecyclerView.Adapter<PictureHolder>()  {

    var album = mutableListOf<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.picture_recycler, parent, false)
        return PictureHolder(view)
    }

    override fun getItemCount(): Int {
        return album.size
    }

    override fun onBindViewHolder(holder: PictureHolder, position: Int) {
        val pictureUri = album.get(position)
        holder.setPicture(pictureUri)
    }


}

class PictureHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun setPicture(pictureUri : Uri) {
        itemView.pictureInAlbum.setImageURI(pictureUri)

    }
}