package team.marker.view.pick.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_history.view.title
import kotlinx.android.synthetic.main.item_pick_product.view.*
import team.marker.R
import team.marker.model.responses.Product

class PickProductsAdapter(
    private val items: MutableList<Product>,
    private val clickListener: (Product) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_pick_product, parent, false)
        return PickProductsViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.title.text = "${item.title}"
        holder.itemView.manufacturer.text = "${item.manufacturer?.title}"
        holder.itemView.setOnClickListener { clickListener(item) }
    }

    class PickProductsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val title: TextView = view.title
    }

    fun clear() {
        val size: Int = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}