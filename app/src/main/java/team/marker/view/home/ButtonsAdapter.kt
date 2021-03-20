package team.marker.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_home_btn.view.*
import team.marker.R
import team.marker.model.HomeButton

class ButtonsAdapter(
    private val buttons: List<HomeButton>,
    private val clickListener: (Int) -> Unit
) : RecyclerView.Adapter<ButtonsAdapter.ButtonsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ButtonsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_home_btn, parent, false)
    )

    override fun getItemCount() = buttons.size

    override fun onBindViewHolder(holder: ButtonsViewHolder, position: Int) {
        val item = buttons[position]
        with(holder) {
            icon.setImageResource(item.iconRes)
            itemView.title.text = item.title
            itemView.message.text = item.message
            itemView.setOnClickListener { clickListener(position) }
        }
    }

    class ButtonsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var icon: ImageView = view.icon
        var title: TextView = view.title
        var message: TextView = view.message
    }
}