package com.example.mydiary

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class DiaryAdapter(options: FirebaseRecyclerOptions<Diary>) :
    FirebaseRecyclerAdapter<Diary, DiaryAdapter.MyViewHolder>(options) {

    private var itemList: List<Diary>? = null
    private var clickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(diary: Diary, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Diary) {
        val item = getItem(position)
        holder.title.text = item.title
        holder.date.text = item.date
        holder.time.text = item.time

        // Validate and set the background color of the diary container
        if (item.diaryColor.isNotEmpty() && isValidColor(item.diaryColor)) {
            val color = Color.parseColor(item.diaryColor)
            holder.diaryContainer.setBackgroundColor(color)
        } else {
            holder.diaryContainer.setBackgroundColor(Color.WHITE)
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.diaryblock, parent, false)
        return MyViewHolder(itemView, clickListener)
    }

    fun searchItemList(searchItem: ArrayList<Diary>) {
        itemList = searchItem
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: super.getItemCount()
    }

    override fun getItem(position: Int): Diary {
        return itemList?.get(position) ?: super.getItem(position)
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
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(diary, position)
                }
            }
        }
    }
}

