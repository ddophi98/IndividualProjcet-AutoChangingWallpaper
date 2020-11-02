package kr.co.ddophi.autochangingwallpaper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler.view.*

class CustomAdapter(recyclerViewInterface: MyRecyclerViewInterface) : RecyclerView.Adapter<Holder>() {

    var albumData = mutableListOf<Album>()
    var recyclerViewInterface : MyRecyclerViewInterface? = null

    init{
        this.recyclerViewInterface = recyclerViewInterface
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler, parent, false)
        return Holder(view, this.recyclerViewInterface!!)
    }

    override fun getItemCount(): Int {
        return albumData.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val album = albumData.get(position)
        holder.setAlbum(album)
    }

}

 class Holder(itemView: View, recyclerViewInterface: MyRecyclerViewInterface) : RecyclerView.ViewHolder(itemView) {

     init{
         itemView.setOnClickListener { recyclerViewInterface.ItemClicked(adapterPosition) }
         itemView.btnDelete.setOnClickListener { recyclerViewInterface.DeleteButtonClicked(adapterPosition) }
     }

    fun setAlbum(album : Album) {
        itemView.albumTitle.text = album.albumTitle
        itemView.pictureCount.text = "${album.pictureCount}"
        itemView.albumImage.setImageURI(album.albumImages[0])
    }




}