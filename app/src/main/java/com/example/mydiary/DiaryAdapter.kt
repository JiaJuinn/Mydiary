package com.example.mydiary

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class DiaryAdapter(options: FirebaseRecyclerOptions<Diary>) :
    FirebaseRecyclerAdapter<Diary, DiaryAdapter.MyViewHolder>(options) {

    private var clickListener: OnItemClickListener? = null
    private var itemList: List<Diary>? = null

    interface OnItemClickListener {
        fun onItemClick(diary: Diary, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Diary) {
        holder.title.text = model.title
        holder.date.text = model.date
        holder.time.text = model.time

        // Validate and set the background color of the diary container
        if (model.diaryColor.isNotEmpty() && isValidColor(model.diaryColor)) {
            val color = Color.parseColor(model.diaryColor)
            holder.diaryContainer.setBackgroundColor(color)
        } else {
            // Set a default color if the color string is invalid
            holder.diaryContainer.setBackgroundColor(Color.WHITE) // or any default color
        }

        holder.bind(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.diaryblock, parent, false)
        return MyViewHolder(itemView, clickListener)
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: super.getItemCount()
    }

    override fun onDataChanged() {
        super.onDataChanged()
        notifyDataSetChanged()
    }

    private fun isValidColor(colorString: String): Boolean {
        return try {
            Color.parseColor(colorString)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    class MyViewHolder(itemView: View, private val listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.diaryTitle)
        val date: TextView = itemView.findViewById(R.id.diaryDate)
        val time: TextView = itemView.findViewById(R.id.diaryTime)
        val diaryContainer: View = itemView.findViewById(R.id.diary_container)

        fun bind(diary: Diary) {
            itemView.setOnClickListener {
                val position = adapterPosition
                Log.d("DiaryAdapter", "Clicked position: $position")
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(diary, position)
                }
            }
        }
    }
}
