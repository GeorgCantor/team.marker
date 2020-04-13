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
    options: MutableList<ProductOption>,
    private val clickListener: (ProductOption) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val options = mutableListOf<ProductOption>()

    init {
        this.options.addAll(options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_product_option, null, false)
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rootView.layoutParams = lp
        return ProductOptionsViewHolder(rootView)

        //return ProductOptionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_product_option, null))
    }

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val option = options[position]

        var title = "${option.title}"
        if (!option.units_title.isNullOrEmpty()) title += ", ${option.units_title}"
        holder.itemView.title_tv.text = title
        holder.itemView.value_tv.text = "${option.value}"

        /*when (holder) {
            is WithdrawsViewHolder -> {
                val status = withdraw.getStatus()

                holder.time.text = withdraw.hours
                holder.amount.text = withdraw.amount
                holder.status.text = status

                holder.itemView.setOnClickListener { clickListener(withdraw) }
            }
            is DateViewHolder -> {
                holder.date.text = withdraw.getDate()
            }
        }*/
    }

    class ProductOptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title_tv
    }

}