package com.example.agriconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agriconnect.chatfiles.ChatItem
import com.example.agriconnect.R


class InboxAdapter(
    private val chatList: List<ChatItem>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<InboxAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.usernameTextView)
        val lastMessageText: TextView = itemView.findViewById(R.id.lastMessageTextView)
        val profileImage: ImageView = itemView.findViewById(R.id.profileImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_inbox, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        holder.usernameText.text = chat.username
        holder.lastMessageText.text = chat.lastMessage
        // TODO: Load profile image if available using Glide/Picasso
        holder.itemView.setOnClickListener { onClick(chat.chatId) }
    }

    override fun getItemCount(): Int = chatList.size
}
