package team.marker.view.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product_file.view.*
import kotlinx.android.synthetic.main.item_product_option.view.title_tv
import team.marker.R
import team.marker.model.responses.ProductFile


class ProductFilesAdapter(
    options: MutableList<ProductFile>,
    private val clickListener: (ProductFile) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val files = mutableListOf<ProductFile>()

    init {
        this.files.addAll(options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_product_file, null, false)
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rootView.layoutParams = lp
        return ProductOptionsViewHolder(rootView)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = files[position]
        val type = file.type

        holder.itemView.title_tv.text = "${file.title}"

        when (type) {
            1 -> holder.itemView.icon_iv.setImageResource(R.drawable.ic_pdf)
            2 -> holder.itemView.icon_iv.setImageResource(R.drawable.ic_word)
            3 -> holder.itemView.icon_iv.setImageResource(R.drawable.ic_excel)
            4 -> holder.itemView.icon_iv.setImageResource(R.drawable.ic_txt)
            5 -> holder.itemView.icon_iv.setImageResource(R.drawable.ic_jpg)
        }

        holder.itemView.setOnClickListener { clickListener(file) }

    }

    class ProductOptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title_tv
    }

}