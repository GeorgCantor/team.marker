package team.marker.view.breach.complete

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_photo.view.*
import team.marker.R
import java.io.File

class PhotosAdapter(
    private val photos: List<File>
) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PhotosViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
    )

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        val file = photos[position]
        with(holder) {
            Glide.with(itemView.context)
                .load(file)
                .into(photo)
        }
    }

    override fun getItemCount() = photos.size

    class PhotosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.photo
    }
}