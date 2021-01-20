package team.marker.view.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product_file.view.*
import team.marker.R
import team.marker.model.responses.ProductFile

class ProductFilesAdapter(
    private val files: MutableList<ProductFile>,
    private val clickListener: (ProductFile) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductOptionsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_product_file, parent, false)
    )

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = files[position]

        with(holder) {
            itemView.title_tv.text = "${file.title}"

            itemView.icon_iv.setImageResource(
                when (file.type) {
                    1 -> R.drawable.ic_pdf
                    2 -> R.drawable.ic_word
                    3 -> R.drawable.ic_excel
                    4 -> R.drawable.ic_txt
                    5 -> R.drawable.ic_jpg
                    else -> R.drawable.transparent
                }
            )

            itemView.setOnClickListener { clickListener(file) }
        }
    }

    class ProductOptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title_tv
    }
}