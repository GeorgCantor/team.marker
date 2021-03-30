package team.marker.view.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product_option.view.*
import team.marker.R
import team.marker.model.responses.ProductOption

class ProductOptionsAdapter(
    private val options: MutableList<ProductOption>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductOptionsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_product_option, parent, false)
    )

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val option = options[position]
        var title = option.title
        if (!option.units_title.isNullOrEmpty()) title += ", ${option.units_title}"
        holder.itemView.title_tv.text = title
        holder.itemView.value_tv.text = option.value
    }

    class ProductOptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title_tv
    }
}