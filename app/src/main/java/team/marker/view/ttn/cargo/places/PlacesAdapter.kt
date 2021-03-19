package team.marker.view.ttn.cargo.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_place.view.*
import team.marker.R
import team.marker.model.responses.Product

class PlacesAdapter(
    private val items: List<Product>,
    private val clickListener: (Product) -> Unit
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PlacesViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val item = items[position]
        with(holder) {
            product = item
            itemView.title.text = "${item.title}"
            itemView.quantity.text = "${item.manufacturer?.title}"
        }
    }

    inner class PlacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var product: Product? = null

        init {
            view.setOnClickListener { product?.let { clickListener(it) } }
        }
    }
}