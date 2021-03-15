package com.example.messanger.MainSite;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.messanger.Chat.ChatActivity;
import com.example.messanger.Models.Message;
import com.example.messanger.Models.User;
import com.example.messanger.Profile.UserProfileActivity;
import com.example.messanger.R;
import com.example.messanger.Repositories.DataBase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Michal Malek
 * Class for Display friends, invites, conversations, search list and configure all functionality
 * - accept/decline invite from user
 * - add user after accept
 * - get photo profile logged user from FirebaseStorage
 * - live searching database and display results
 */


public class MainSite extends AppCompatActivity {

    private ListView listView, listOfconversations, listOfInvites, listOfFriends;
    private TextView conversationsLabel, contactsLabel, invitesLabel;
    private SearchView searchView;
    private List<String> mails;
    private ArrayAdapter<String> arrayAdapter;
    private CircleImageView profilePhoto;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DataBase dataBase;

    private ObjectAnimator animator;
    private AnimatorSet animatorSet;

    String userLoggedKey = null;
    Map<String, User> all = new HashMap<>();

    String lastSite = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_site);

        searchView = findViewById(R.id.svSearchBar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dataBase = new DataBase();

        conversationsLabel = findViewById(R.id.tvConversations);
        listOfconversations = findViewById(R.id.lvConversations);

        listOfFriends = findViewById(R.id.lvListOfFriends);
        contactsLabel = findViewById(R.id.tvContacts);

        listOfInvites = findViewById(R.id.lvInvites);
        invitesLabel = findViewById(R.id.tvInvites);

        profilePhoto = findViewById(R.id.ivProfilePhoto);

        getUserPhoto();
        getFriendList();
        getListOfInvites();
        configSearchView();

        DatabaseReference userRef = dataBase.firebase.getReference().child("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    if (s.getValue(User.class).getEmail().equals(getEmailActualLoggedUser())) {
                        userLoggedKey = s.getKey();
                    }
                    all.put(s.getKey(), s.getValue(User.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        getConversation();

    }

    private void getConversation() {
        conversationsLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastSite = "conversations";

                conversationsLabel.setTextColor(Color.rgb(200, 48, 48));
                contactsLabel.setTextColor(Color.rgb(128,128,128));
                invitesLabel.setTextColor(Color.rgb(128,128,128));

                listOfconversations.setVisibility(View.VISIBLE);
                listOfInvites.setVisibility(View.GONE);
                listOfFriends.setVisibility(View.GONE);
                DatabaseReference convRef = dataBase.firebase.getReference().child("Conversations");
                convRef.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        System.out.println("ładuje konwersacje");
                        Map<String, List<Message>> active = new HashMap<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            if (s.getKey().split("-")[1].equals(userLoggedKey)) {
                                List<Message> mess = new ArrayList<>();
                                for (DataSnapshot x : s.getChildren()) {
                                    mess.add(x.getValue(Message.class));
                                }
                                active.put(s.getKey().split("-")[0], mess);
                            }
                        }

                        List<String> names = active.keySet().stream().map(i -> all.get(i).getName() + " " + all.get(i).getSurName()).collect(Collectors.toList());
                        String[] arrNames = Arrays.copyOf(names.toArray(), names.toArray().length, String[].class);

                        List<String> images = active.keySet().stream().map(i -> all.get(i).getProfilePhotoID()).collect(Collectors.toList());
                        String[] arrImages = Arrays.copyOf(images.toArray(), images.toArray().length, String[].class);

                        List<String> lastMessages = active.keySet().stream().map(i -> active.get(i).get(active.get(i).size()-1).getText()).collect(Collectors.toList());
                        String[] arrLastMessages = Arrays.copyOf(lastMessages.toArray(), lastMessages.toArray().length, String[].class);

                        List<String> mails = active.keySet().stream().map(i -> all.get(i).getEmail()).collect(Collectors.toList());


                        ConversationAdapter conversationAdapter = new ConversationAdapter(MainSite.this, arrImages, arrNames, arrLastMessages);
                        listOfconversations.setAdapter(conversationAdapter);

                        // todo on click row listener
                        listOfconversations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                User userLogged = null;
                                String keyUserLogged = null;

                                User userClicked = null;
                                String keyUserClicked = null;

                                for (String key : all.keySet()) {
                                    if (all.get(key).getEmail().equals(getEmailActualLoggedUser())) {
                                        userLogged = all.get(key);
                                        keyUserLogged = key;
                                    } else if (all.get(key).getEmail().equals(mails.get(position))) {
                                        userClicked = all.get(key);
                                        keyUserClicked = key;
                                    }
                                }
                                Intent intent = new Intent(MainSite.this, ChatActivity.class);
                                intent.putExtra("userLogged", userLogged);
                                intent.putExtra("keyUserLogged", keyUserLogged);
                                intent.putExtra("keyUserClicked", keyUserClicked);
                                intent.putExtra("userClicked", userClicked);
                                startActivity(intent);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        conversationsLabel.performClick();

    }

    /**
     * Method using before created FriendAdapter class
     * Get data from database and put in FriendAdapter
     */
    private void getFriendList() {
        contactsLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastSite = "friends";

                contactsLabel.setTextColor(Color.rgb(200, 48, 48));
                conversationsLabel.setTextColor(Color.rgb(128,128,128));
                invitesLabel.setTextColor(Color.rgb(128,128,128));

                listOfconversations.setVisibility(View.GONE);
                listOfInvites.setVisibility(View.GONE);
                listOfFriends.setVisibility(View.VISIBLE);

                searchView.clearFocus();

                DatabaseReference databaseReference = dataBase.firebase.getReference().child("Users");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> all = new ArrayList<>();
                        snapshot.getChildren().forEach(i -> all.add(i.getValue(User.class)));

                        Map<String, User> keysWithUser = new HashMap<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            keysWithUser.put(s.getKey(), s.getValue(User.class));
                        }

                        List<Integer> friendsID = all.stream().filter(i -> i.getEmail().equals(getEmailActualLoggedUser())).findFirst().get().getFriends().getListFriends();
                        List<User> friendsObjects = friendsID.subList(1, friendsID.size()).stream().map(i -> all.get(i)).collect(Collectors.toList());

                        Object[] arrTitles = friendsObjects.stream().map(i -> i.getName() + " " + i.getSurName()).toArray();
                        String[] titles = Arrays.copyOf(arrTitles, arrTitles.length, String[].class);

                        Object[] arrImages = friendsObjects.stream().map(i -> i.getProfilePhotoID()).toArray();
                        String[] images = Arrays.copyOf(arrImages, arrImages.length, String[].class);

                        Object[] arrMails = friendsObjects.stream().map(i -> i.getEmail()).toArray();
                        String[] mails = Arrays.copyOf(arrMails, arrMails.length, String[].class);

                        FriendAdapter friendAdapter = new FriendAdapter(MainSite.this, titles, images, mails);
                        listOfFriends.setAdapter(friendAdapter);

                        listOfFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                User userLogged = all.stream().filter(i -> i.getEmail().equals(getEmailActualLoggedUser())).findFirst().get();
                                String keyUserLogged = keysWithUser.keySet().stream().filter(i -> keysWithUser.get(i).getEmail().equals(userLogged.getEmail())).findFirst().get();

                                User userClicked = all.stream().filter(i -> i.getEmail().equals(mails[position])).findFirst().get();
                                String keyUserClicked = keysWithUser.keySet().stream().filter(i -> keysWithUser.get(i).getEmail().equals(userClicked.getEmail())).findFirst().get();

                                Intent intent = new Intent(MainSite.this, ChatActivity.class);
                                intent.putExtra("userLogged", userLogged);
                                intent.putExtra("keyUserLogged", keyUserLogged);
                                intent.putExtra("keyUserClicked", keyUserClicked);
                                intent.putExtra("userClicked", userClicked);

                                startActivity(intent);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    /**
     * Searching by email bacause email is UNIQUE in this application
     * Implement onItemClick method which launch new activity with details before clicked user
     * List View use basic ArrayAdapter based on string values (e-mails)
     * Implement onQueryTextChange method which search whole time database and gives results on screen
     */
    private void configSearchView() {
        listView = findViewById(R.id.lvQueryResult);
        mails = new ArrayList<>();
        List<User> users = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mails.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    User user = s.getValue(User.class);
                    mails.add(user.getEmail());
                    users.add(user);
                }
                arrayAdapter = new ArrayAdapter<String>(MainSite.this, android.R.layout.simple_list_item_1, mails);
                listView.setAdapter(arrayAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent showProfileDetails = new Intent(MainSite.this, UserProfileActivity.class);
                        User selectedUser = users.stream().filter(u -> u.getEmail().equals(arrayAdapter.getItem(position))).findFirst().get();
                        // SEND SELECTED USER NEED TO SERIALIZABLE INTERFACE IMPLEMENTED
                        showProfileDetails.putExtra("selectedUser", selectedUser);
                        startActivity(showProfileDetails);
                    }
                });

                listView.setVisibility(View.GONE);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.equals("")) {
                            switch (lastSite) {
                                case "conversations":
                                    animator = ObjectAnimator.ofFloat(listOfconversations, "y", 500f);
                                    animator.setDuration(250);
                                    animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animator);
                                    animatorSet.start();
                                    break;
                                case "friends":
                                    animator = ObjectAnimator.ofFloat(listOfFriends, "y", 500f);
                                    animator.setDuration(250);
                                    animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animator);
                                    animatorSet.start();
                                    break;
                                case "invites":
                                    animator = ObjectAnimator.ofFloat(listOfInvites, "y", 500f);
                                    animator.setDuration(250);
                                    animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animator);
                                    animatorSet.start();
                                    break;
                            }
                            listView.setVisibility(View.GONE);
                            conversationsLabel.setVisibility(View.VISIBLE);
                            contactsLabel.setVisibility(View.VISIBLE);
                            invitesLabel.setVisibility(View.VISIBLE);
                        } else {

                            switch (lastSite) {
                                case "conversations":
                                    animator = ObjectAnimator.ofFloat(listOfconversations, "y", 800f);
                                    animator.setDuration(250);
                                    animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animator);
                                    animatorSet.start();
                                    break;
                                case "friends":
                                    animator = ObjectAnimator.ofFloat(listOfFriends, "y", 800f);
                                    animator.setDuration(250);
                                    animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animator);
                                    animatorSet.start();
                                    break;
                                case "invites":
                                    animator = ObjectAnimator.ofFloat(listOfInvites, "y", 800f);
                                    animator.setDuration(250);
                                    animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(animator);
                                    animatorSet.start();
                                    break;
                            }




                            listView.setVisibility(View.VISIBLE);
                            conversationsLabel.setVisibility(View.GONE);
                            contactsLabel.setVisibility(View.GONE);
                            invitesLabel.setVisibility(View.GONE);
                        }
                        arrayAdapter.getFilter().filter(newText);
                        return true;
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Connect to database search users, if user have email equals as logged user email take photo ID (UUID)
     * and connect to DatabaseStorage where stored all images, find image with exacly same photo ID and load
     * via GLIDE into control on screen (profilePhoto)
     */
    private void getUserPhoto() {

        DatabaseReference databaseReference = dataBase.firebase.getReference().child("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    if (s.getValue(User.class).getEmail().equals(getEmailActualLoggedUser())) {
                        String idPhoto = s.getValue(User.class).getProfilePhotoID();
                        firebaseStorage = FirebaseStorage.getInstance();
                        storageReference = firebaseStorage.getReference().child("images/" + idPhoto);
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(getApplicationContext()).load(uri).into(profilePhoto);
                            }
                        });
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    /**
     * Method using InviteAdapter
     * Connect to database and get data from logged user about invites
     */
    private void getListOfInvites() {
        invitesLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastSite = "invites";

                searchView.clearFocus();

                invitesLabel.setTextColor(Color.rgb(200, 48, 48));
                conversationsLabel.setTextColor(Color.rgb(128,128,128));
                contactsLabel.setTextColor(Color.rgb(128,128,128));

                listOfconversations.setVisibility(View.GONE);
                listOfFriends.setVisibility(View.GONE);
                listOfInvites.setVisibility(View.VISIBLE);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> allUsers = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            User user = s.getValue(User.class);
                            if (user.getInvites() != null)
                                allUsers.add(user);
                        }
                        List<Integer> indexInvites = allUsers.stream().filter(i -> i.getEmail().equals(getEmailActualLoggedUser()))
                                .findFirst().get().getInvites().getInvites();
                        List<User> inviteRequests = indexInvites.subList(1, indexInvites.size()).stream().map(x -> allUsers.get(x)).collect(Collectors.toList());

                        Object[] arrTitles = inviteRequests.stream().map(i -> i.getName() + " " + i.getSurName()).toArray();
                        String[] titles = Arrays.copyOf(arrTitles, arrTitles.length, String[].class);

                        Object[] arrImages = inviteRequests.stream().map(i -> i.getProfilePhotoID()).toArray();
                        String[] images = Arrays.copyOf(arrImages, arrImages.length, String[].class);

                        Object[] arrMails = inviteRequests.stream().map(i -> i.getEmail()).toArray();
                        String[] mails = Arrays.copyOf(arrMails, arrMails.length, String[].class);

                        InviteAdapter inviteAdapter = new InviteAdapter(MainSite.this, titles, images, mails);
                        listOfInvites.setAdapter(inviteAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private String getEmailActualLoggedUser() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("email");
    }


}