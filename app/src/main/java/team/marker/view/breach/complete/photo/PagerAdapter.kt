package team.marker.view.breach.complete.photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_photo_pager.view.*
import team.marker.R
import team.marker.util.loadPhoto
import java.io.File

class PagerAdapter(private val files: List<File>) : RecyclerView.Adapter<PagerAdapter.PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_photo_pager, parent, false)
    )

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.itemView.context.loadPhoto(files[position], holder.photo)
    }

    override fun getItemCount() = files.size

    class PagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.image
    }
}