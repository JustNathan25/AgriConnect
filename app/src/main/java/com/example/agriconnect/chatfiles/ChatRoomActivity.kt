package com.example.agriconnect.chatfiles

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriconnect.chatfiles.MessageAdapter
import com.example.agriconnect.R
import com.example.agriconnect.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var chatId: String? = null
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        chatId = intent.getStringExtra("chatId")

        recyclerView = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(messageList)
        recyclerView.adapter = adapter

        btnSend.setOnClickListener { sendMessage() }
        loadMessages()
    }

    private fun loadMessages() {
        chatId?.let {
            db.collection("chats").document(it).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null || snapshots == null) return@addSnapshotListener
                    messageList.clear()
                    for (doc in snapshots) {
                        val msg = doc.toObject(Message::class.java)
                        messageList.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
        }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return
        if (text.isEmpty()) return

        val msg = Message(
            senderId = userId,
            text = text,
            timestamp = Timestamp.Companion.now()
        )

        chatId?.let {
            val chatRef = db.collection("chats").document(it)
            chatRef.collection("messages").add(msg)
            chatRef.update(
                mapOf(
                    "lastMessage" to text,
                    "lastTimestamp" to Timestamp.Companion.now()
                )
            )
        }

        etMessage.text.clear()
    }
}