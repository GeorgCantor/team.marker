package team.marker.view.ttn.cargo.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_place.view.*
import team.marker.R
import team.marker.model.ttn.ProductPlace

class PlacesAdapter(
    private val items: List<ProductPlace>,
    private val clickListener: (ProductPlace) -> Unit
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PlacesViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val item = items[position]
        with(holder) {
            place = item
            itemView.title.text = itemView.context.resources.getString(R.string.cargo_place, position + 1)
            itemView.quantity.text = itemView.context.resources.getString(R.string.cargo_quantity, item.products.size)
        }
    }

    inner class PlacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var place: ProductPlace? = null

        init {
            view.setOnClickListener { place?.let { clickListener(it) } }
        }
    }
}