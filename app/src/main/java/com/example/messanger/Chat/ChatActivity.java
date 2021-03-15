package com.example.messanger.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.messanger.Models.Message;
import com.example.messanger.Models.User;
import com.example.messanger.R;
import com.example.messanger.Repositories.DataBase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView profilePhoto;
    private TextView fullName;
    private Button sendMessage;
    private EditText messageText;
    private DataBase dataBase;
    private ListView messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messages = findViewById(R.id.lvMessages);
        sendMessage = findViewById(R.id.btnSendMessage);
        messageText = findViewById(R.id.etMessageToSend);


        System.out.println(messages.getLayoutParams().height);

        configChatWindow();
        displayMessages();
        createChatRoom();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void displayMessages() {
        Bundle bundle = getIntent().getExtras();

        String keyUserLogged = bundle.getString("keyUserLogged");
        String keyUserClicked = bundle.getString("keyUserClicked");

        dataBase = new DataBase();
        DatabaseReference databaseReference = dataBase.firebase.getReference().child("Conversations");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messList = new ArrayList<>();
                String key = keyUserLogged + "-" + keyUserClicked;
                for (DataSnapshot s : snapshot.getChildren()) {
                    if (s.getKey().equals(key)) {
                        for (DataSnapshot x : s.getChildren()) {
                            messList.add(x.getValue(Message.class));
                        }
                    }
                }
                String[] arrMess = Arrays.copyOf(messList.stream().map(i -> i.getSender() + ": "  + i.getText()).toArray(), messList.stream().map(i -> i.getText()).toArray().length, String[].class);
                MessageAdapter messageAdapter = new MessageAdapter(ChatActivity.this, arrMess);
                messages.post(new Runnable() {
                    @Override
                    public void run() {
                        messages.setSelection(messages.getCount() - 1);
                    }
                });
                messages.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private boolean createChatRoom() {
        Bundle bundle = getIntent().getExtras();


        dataBase = new DataBase();
        DatabaseReference conversationsReference = dataBase.firebase.getReference().child("Conversations");
        conversationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userLogged = (User) bundle.get("userLogged");
                String keyUserLogged = bundle.getString("keyUserLogged");

                User userClicked = (User) bundle.get("userClicked");
                String keyUserClicked = bundle.getString("keyUserClicked");

                String existingConversation = keyUserLogged + "-" + keyUserClicked;
                String reverseConversation = keyUserClicked + "-" + keyUserLogged;
                for (DataSnapshot s : snapshot.getChildren()) {
                    if (s.getKey().equals(existingConversation)) {
                        for (DataSnapshot x : s.getChildren()) {
                            System.out.println(x.getValue(Message.class));
                        }
                        break;
                    }
                }

                sendMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference messRef = dataBase.firebase.getReference().child("Conversations");
                        messRef.child(existingConversation).push().setValue(new Message(userLogged.getName() + " " + userLogged.getSurName(), userClicked.getName() + " " + userClicked.getSurName(), messageText.getText().toString()));
                        messRef.child(reverseConversation).push().setValue(new Message(userLogged.getName() + " " + userLogged.getSurName(), userClicked.getName() + " " + userClicked.getSurName(), messageText.getText().toString()));
                        messageText.setText("");
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return false;
    }

    private void configChatWindow() {
        profilePhoto = findViewById(R.id.ivProfilePhoto);
        fullName = findViewById(R.id.tvFullName);

        Bundle bundle = getIntent().getExtras();
        User userClicked = (User) bundle.get("userClicked");
        fullName.setText(userClicked.getName() + " " + userClicked.getSurName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + userClicked.getProfilePhotoID());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this).load(uri).into(profilePhoto);
            }
        });

    }
}