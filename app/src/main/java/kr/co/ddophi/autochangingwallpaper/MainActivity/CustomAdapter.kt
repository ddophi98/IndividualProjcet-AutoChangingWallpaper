package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler.view.*
import kr.co.ddophi.autochangingwallpaper.Album
import kr.co.ddophi.autochangingwallpaper.R

//여러 앨범들 보여주는 리사이클러뷰 구현
class CustomAdapter(recyclerViewInterface: MyRecyclerViewInterface) : RecyclerView.Adapter<CustomAdapter.Holder>() {

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
        val album = albumData[position]
        holder.setAlbum(album)
    }

    class Holder(itemView: View, recyclerViewInterface: MyRecyclerViewInterface) : RecyclerView.ViewHolder(itemView) {

        init{
            itemView.btnDelete.setOnClickListener { recyclerViewInterface.DeleteButtonClicked(adapterPosition) }
            itemView.btnEdit.setOnClickListener {recyclerViewInterface.EditButtonClicked(adapterPosition)}
            itemView.btnSelect.setOnClickListener {recyclerViewInterface.SelectButtonClicked(adapterPosition)}
        }

        fun setAlbum(album : Album) {
            itemView.albumTitle.text = album.albumTitle
            itemView.pictureCount.text = "${album.pictureCount}"
            itemView.albumImage.setImageURI(album.representImage)
        }
    }

}

