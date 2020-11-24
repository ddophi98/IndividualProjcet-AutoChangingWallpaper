package kr.co.ddophi.autochangingwallpaper.EditActivity

import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.picture_recycler.view.*
import kr.co.ddophi.autochangingwallpaper.R

//여러 사진들 보여주는 리사이클러뷰 구현
class EditAlbumAdapter(val activity: AppCompatActivity, val title: String?, val album: MutableList<Uri>, var representImage : Uri?) : RecyclerView.Adapter<EditAlbumAdapter.PictureHolder>(){

    var selectedList = mutableListOf<Uri>()
    var isEditMode = false
    lateinit var actionMode: ActionMode

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.picture_recycler, parent, false)
        return PictureHolder(view)
    }

    override fun getItemCount(): Int {
        return album.size
    }

    override fun onBindViewHolder(holder: PictureHolder, position: Int) {
        val pictureUri = album[position]
        //각각의 사진들 보이도록 연결해주기

        holder.setPicture(pictureUri)

        //사진 길게 눌렀을 때 동작
        holder.itemView.setOnLongClickListener{

            if(!isEditMode) {
                //편집 모드가 아니라면 편집 모드로 진입
                val callback = object : ActionMode.Callback {

                    //메뉴 아이템 클릭될 때 동작
                    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                        when (item?.itemId) {

                            //삭제 버튼 클릭
                            R.id.btnDeletePictures -> {
                                if (selectedList.size == album.size) {
                                    Toast.makeText(activity, "최소 한개의 사진은 남아있어야합니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    showDeletePopup(mode)
                                }
                            }

                            //모두 선택 버튼 클릭
                            R.id.btnSelectAll -> {
                                if (selectedList.size == album.size) {
                                    selectedList.clear()
                                    mode?.title = "${selectedList.size}"
                                } else {
                                    selectedList.clear()
                                    selectedList.addAll(album)
                                    mode?.title = "${selectedList.size}"
                                }
                                notifyDataSetChanged()
                            }

                            //대표 사진 버튼 클릭
                            R.id.btnRepresent -> {
                                when {
                                    selectedList.size == 1 -> {
                                        representImage = selectedList[0]
                                        holder.itemView.iconRepresent.visibility = View.VISIBLE
                                        mode?.finish()
                                    }
                                    selectedList.size > 1 -> {
                                        Toast.makeText(activity, "대표 사진은 한개만 선택해야합니다.", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        Toast.makeText(activity, "대표 사진은 적어도 한개는 선택해야합니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        return true
                    }

                    //메뉴 생성
                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        val inflater: MenuInflater = mode!!.menuInflater
                        inflater.inflate(R.menu.edit_menu, menu)
                        actionMode = mode
                        return true
                    }

                    //길게 눌렀던 사진 선택 및 편집 모드 진입 표시
                    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        isEditMode = true
                        selectItem(holder)
                        return true
                    }

                    //편집 모드에서 나올 때 동작
                    override fun onDestroyActionMode(mode: ActionMode?) {
                        isEditMode = false
                        selectedList.clear()
                        notifyDataSetChanged()
                    }
                }
                activity.startSupportActionMode(callback)
            }else{
                //편집모드라면 그냥 사진 선택
                selectItem(holder)
            }
            true
        }

        //사진 살짝 눌렀을 때 동작
        holder.itemView.setOnClickListener{
            if(isEditMode){
                //편집 모드라면 그냥 사진 선택
                selectItem(holder)
            }else{
                //편집 모드가 아니라면 사진 확대
                val enlargePicture = Intent(activity, PictureShowActivity::class.java)
                enlargePicture.putExtra("Uri", pictureUri)
                activity.startActivity(enlargePicture)
            }
        }
    }

    //사진 선택
    fun selectItem(holder : RecyclerView.ViewHolder) {
        val pictureUri = album[holder.adapterPosition]

        if(holder.itemView.iconChecked.visibility == View.INVISIBLE){
            holder.itemView.iconChecked.visibility = View.VISIBLE
            holder.itemView.pictureInAlbum.alpha = 0.3f
            selectedList.add(pictureUri)
        }else{
            holder.itemView.iconChecked.visibility = View.INVISIBLE
            holder.itemView.pictureInAlbum.alpha = 1.0f
            selectedList.remove(pictureUri)
        }
        actionMode.title = "${selectedList.size}"
    }

    //선택한 사진들 삭제할 때 나오는 팝업 메시지
    fun showDeletePopup(mode : ActionMode?) {
        val alertDialog = AlertDialog.Builder(activity)
            .setTitle("사진 삭제")
            .setMessage("${selectedList.size}개의 사진을 정말 삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->

                if(selectedList.contains(representImage)){
                    representImage = if(representImage == album[0]){
                        album[1]
                    }else {
                        album[0]
                    }
                }

                for (pic in selectedList) {
                    album.remove(pic)
                }
                mode?.finish()
                Toast.makeText(activity, "사진이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("취소", null)
            .create()

        alertDialog.show()
    }

    inner class PictureHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setPicture(pictureUri : Uri) {
            Glide.with(activity).load(pictureUri).placeholder(R.drawable.progress_animation).into(itemView.pictureInAlbum)
            //선택된 사진인지 수시로 확인
            if(selectedList.contains(pictureUri)){
                itemView.iconChecked.visibility = View.VISIBLE
                itemView.pictureInAlbum.alpha = 0.3f
            }else{
                itemView.iconChecked.visibility = View.INVISIBLE
                itemView.pictureInAlbum.alpha = 1.0f
            }

            //대표 사진인지 수시로 확인
            if(pictureUri != representImage){
                itemView.iconRepresent.visibility = View.INVISIBLE
            }else{
                itemView.iconRepresent.visibility = View.VISIBLE
            }
        }
    }
}

