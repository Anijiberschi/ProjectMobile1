package com.example.projectmobile1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private DatabaseReference userRef;
    private ValueEventListener userValueEventListener;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private List<ChatMessage> userMessages;
    private ChatAdapter chatAdapter;
    private DatabaseReference messagesRef;

    private TextView txtName, txtCommentary;
    private ImageView imageView;
    private RequestQueue requestQueue;
    private String imageUrl = "https://picsum.photos/seed/";
    private Button bt_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);

        userMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(userMessages);

        txtName = findViewById(R.id.txtName);
        txtCommentary = findViewById(R.id.txtCommentary);
        imageView = findViewById(R.id.imageViewProfile);
        bt_logout = findViewById(R.id.bt_Logout);

        requestQueue = Volley.newRequestQueue(this);

        String userId = currentUser.getUid();
        String sender = currentUser.getEmail();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        bt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Profile.this, "Logout", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        messagesRef.orderByChild("sender").equalTo(sender).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                if (chatMessage != null && chatMessage.getSender().equals(sender)) {
                    userMessages.add(chatMessage);
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase Data", "Data changed");
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        Log.d("Firebase User", "User: " + user.toString());
                        String name = user.getName();
                        txtName.setText(name);
                        String description = user.getDescription();
                        txtCommentary.setText(description);
                        String profilePicture = imageUrl + description + "/300/300";
                        loadImageFromUrl(profilePicture);
                    } else {
                        Log.d("Firebase User", "User is null");
                    }
                } else {
                    Log.d("Firebase Data", "Snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                int errorCode = databaseError.getCode();
                String errorMessage = databaseError.getMessage();
                System.out.println("Error occurred: " + errorMessage);
                Log.e("Firebase Error", errorMessage);
            }
        };
    }

    private void loadImageFromUrl(String url) {
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageView.setImageBitmap(response);
            }
        }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
            }
        });

        requestQueue.add(imageRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.addValueEventListener(userValueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userValueEventListener != null) {
            userRef.removeEventListener(userValueEventListener);
        }
    }
}
