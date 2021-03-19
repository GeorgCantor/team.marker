package team.marker.view.ttn.cargo.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product.view.*
import team.marker.R
import team.marker.model.responses.Product

class ProductsAdapter(
    private val items: List<Product>,
    private val clickListener: (Product) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val item = items[position]
        with(holder) {
            product = item
            itemView.title.text = "${item.title}"
            itemView.manufacturer.text = "${item.manufacturer?.title}"
        }
    }

    inner class ProductsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var product: Product? = null

        init {
            view.setOnClickListener { product?.let { clickListener(it) } }
        }
    }
}