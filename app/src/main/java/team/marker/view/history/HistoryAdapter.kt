package team.marker.view.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_history.view.*
import team.marker.R
import team.marker.model.responses.HistoryItem


class HistoryAdapter(
    items: MutableList<HistoryItem>,
    private val clickListener: (HistoryItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HistoryItem>()

    init {
        this.items.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //return HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history, null))

        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.title.text = "${item.title}"
        holder.itemView.created.text = "${item.created}"
        //holder.itemView.setOnClickListener { clickListener(item) }
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val title: TextView = view.title
    }

    fun clear() {
        val size: Int = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}