package team.marker.view.breach.complete

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat.setTransitionName
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_photo.view.*
import team.marker.R
import team.marker.util.loadPhoto
import java.io.File

class PhotosAdapter(
    private val photos: List<File>,
    private val clickListener: (File, FragmentNavigator.Extras) -> Unit
) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PhotosViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
    )

    override fun getItemCount() = photos.size

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        val item = photos[position]
        with(holder) {
            file = item
            itemView.context.loadPhoto(item, photo)
            val extras = FragmentNavigator.Extras.Builder()
                .addSharedElement(photo, file?.name!!)
                .build()
            itemView.setOnClickListener { file?.let { clickListener(it, extras) } }
            setTransitionName(photo, file?.name)
        }
    }

    inner class PhotosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.photo
        var file: File? = null
    }
}