package com.example.projectmobile1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageView sendButton;

    private DatabaseReference messagesRef;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private String textTitle = "Un nouveau message dans le canal !";
    private int notificationId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);



        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Get reference to the "messages" node in the Firebase Realtime Database
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");




        // Listen for new messages
        messagesRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                chatMessages.add(chatMessage);
                chatAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatActivity.this)
                        .setSmallIcon(R.drawable.ic_message_foreground)
                        .setContentTitle(textTitle)
                        .setContentText(chatMessage.getMessage())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);


                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ChatActivity.this);
                notificationManager.notify(notificationId, builder.build()); // notificationId is a unique identifier for this notification
                ++notificationId;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


                if (user != null && !message.isEmpty()) {
                    String sender = user.getEmail();
                    ChatMessage chatMessage = new ChatMessage(message, sender);

                    messagesRef.push().setValue(chatMessage);

                    messageInput.setText("");
                }
            }
        });
}
}