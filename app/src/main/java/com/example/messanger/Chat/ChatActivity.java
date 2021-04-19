package com.example.messanger.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Class ChatActivity is an object that gives us all the functionalities related to chat
 */
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

        profilePhoto = findViewById(R.id.ivProfilePhoto);
        fullName = findViewById(R.id.tvFullName);

        messages = findViewById(R.id.lvMessages);
        sendMessage = findViewById(R.id.btnSendMessage);
        messageText = findViewById(R.id.etMessageToSend);

        configChatWindow();
        displayMessages();
        createChatRoom();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        messages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                ViewGroup.LayoutParams params = messages.getLayoutParams();
                params.height = 1084;
                messages.setLayoutParams(params);
                messages.requestLayout();
                return false;
            }
        });

        messageText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup.LayoutParams params = messages.getLayoutParams();
                System.out.println(params.height);
                params.height = 600;
                messages.setLayoutParams(params);
                messages.requestLayout();
                return false;
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        View v = this.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        ViewGroup.LayoutParams params = messages.getLayoutParams();
        params.height = 1084;
        messages.setLayoutParams(params);
        messages.requestLayout();
        return super.onTouchEvent(event);
    }

    /**
     * Get all messages beetwen users, search key in database if key is correct read all values from this key and display on screen
     */
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

                String[] arrMess = Arrays.copyOf(messList.stream().map(i -> i.getSender() + ": "  + i.getText()).toArray(),
                        messList.stream().map(i -> i.getText()).toArray().length, String[].class);

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


    /**
     * Create chat room in database. If chat room exist add message (value) to key
     * Chat room have key which consists of id of user logged and id of user clicked (user which we want messaging)
     * pattern: id_user_logged-id_user_clicked f.e 1-2 this is a chat room of users with id 1 and 2
     * @return boolean
     */
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

                        String from = userLogged.getCountry();
                        String to = userClicked.getCountry();
                        String mess = messageText.getText().toString().replaceAll(" ", "%20");

                        Thread thread = new Thread(new Runnable() {
                            String translatedMess;
                            @Override
                            public void run() {
                                try {
                                    String link = "https://api.mymemory.translated.net/get?q=" + mess + "&langpair=" + from + "|" + to;
                                    URL url = new URL(link);
                                    URLConnection request = url.openConnection();
                                    request.connect();
                                    JsonParser jsonParser = new JsonParser();
                                    JsonElement jsonElement = jsonParser.parse(new InputStreamReader((InputStream) request.getContent()));
                                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                                    translatedMess = jsonObject.get("responseData").getAsJsonObject().get("translatedText").getAsString();
                                    System.out.println("PRZETLUMACZONE: " + translatedMess);


                                    messRef.child(existingConversation).push().setValue(
                                            new Message(userLogged.getName() + " " + userLogged.getSurName(),
                                                    userClicked.getName() + " " + userClicked.getSurName(),
                                                    messageText.getText().toString()));

                                    messRef.child(reverseConversation).push().setValue(
                                            new Message(userLogged.getName() + " " + userLogged.getSurName(),
                                                    userClicked.getName() + " " + userClicked.getSurName(),
                                                    translatedMess));

                                    messageText.setText("");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();

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


