package com.example.agriconnect.chatfiles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriconnect.chatfiles.ChatRoomActivity
import com.example.agriconnect.InboxAdapter
import com.example.agriconnect.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatInboxFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InboxAdapter
    private val chatList = mutableListOf<ChatItem>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_inbox, container, false)

        recyclerView = view.findViewById(R.id.recyclerChats)
        val fabNewChat = view.findViewById<FloatingActionButton>(R.id.fabNewChat)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = InboxAdapter(chatList) { chatId ->
            safeOpenChatRoom(chatId)
        }
        recyclerView.adapter = adapter

        fabNewChat.setOnClickListener {
            createNewChat()
        }

        loadChats()

        return view
    }

    private fun loadChats() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded || e != null || snapshots == null) return@addSnapshotListener

                chatList.clear()
                for (doc in snapshots) {
                    val chatId = doc.id
                    val lastMessage = doc.getString("lastMessage") ?: ""

                    // Get the other participant's UID to fetch username
                    val participants = doc.get("participants") as? List<*>
                    val otherUserId = participants?.firstOrNull { it != userId } as? String ?: ""

                    val chatItem = ChatItem(chatId, lastMessage, username = "User") // default username

                    if (otherUserId.isNotEmpty() && isAdded) {
                        // Fetch username asynchronously
                        db.collection("users").document(otherUserId).get()
                            .addOnSuccessListener { userDoc ->
                                if (!isAdded) return@addOnSuccessListener
                                val fullName = userDoc.getString("fullName") ?: "User"
                                chatItem.username = fullName
                                adapter.notifyDataSetChanged()
                            }
                    }

                    chatList.add(chatItem)
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun createNewChat() {
        val userId = auth.currentUser?.uid ?: return

        // TODO: Replace with a way to pick/select another user dynamically
        val otherUserId = "op6HaUKW8yUNpJgCmpAcb3ch2Be2" // For testing, set another account UID

        if (otherUserId.isEmpty()) {
            context?.let {
                Toast.makeText(it, "Other user ID missing!", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val chatId = listOf(userId, otherUserId).sorted().joinToString("_")
        val chatRef = db.collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { doc ->
            if (!isAdded) return@addOnSuccessListener

            if (!doc.exists()) {
                val chatData = mapOf(
                    "participants" to listOf(userId, otherUserId),
                    "lastMessage" to "",
                    "lastTimestamp" to null
                )
                chatRef.set(chatData)
            }
            safeOpenChatRoom(chatId)
        }
    }

    private fun safeOpenChatRoom(chatId: String) {
        if (!isAdded) return
        context?.let {
            val intent = Intent(it, ChatRoomActivity::class.java)
            intent.putExtra("chatId", chatId)
            startActivity(intent)
        }
    }
}

// ChatItem data class
data class ChatItem(
    val chatId: String,
    val lastMessage: String,
    var username: String // made var to update after fetching
)
