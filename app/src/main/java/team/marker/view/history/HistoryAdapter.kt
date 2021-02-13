package team.marker.view.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_history.view.*
import team.marker.R
import team.marker.model.responses.HistoryItem

class HistoryAdapter(
    private val items: MutableList<HistoryItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.title.text = "${item.title}"
        holder.itemView.created.text = "${item.created}"
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun clear() {
        val size: Int = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}