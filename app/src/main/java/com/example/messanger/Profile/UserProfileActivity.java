package com.example.messanger.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.messanger.Models.User;
import com.example.messanger.R;
import com.example.messanger.Repositories.DataBase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfileActivity extends AppCompatActivity {

    private CircleImageView selectedProfileImage;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private TextView fullName, phoneNumber, email;
    private Button addToFriends;

    private DataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        selectedProfileImage = findViewById(R.id.selectedProfileImage);

        Bundle bundle = getIntent().getExtras();
        User selectedUser = (User) bundle.get("selectedUser");
        setSelectedUserPhoto(selectedUser);
        setSelectedUserDetails(selectedUser);

        addToFriends = findViewById(R.id.btnAddToFriend);
        addToFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataBase = new DataBase();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Find user id actual logged
                        String idCurrentUserLogged = null;
                        for (DataSnapshot s : snapshot.getChildren()) {
                            if (s.getValue(User.class).getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                idCurrentUserLogged = s.getKey();
                                break;
                            }
                        }
                        // Find list of invites from user which was clicked
                        for (DataSnapshot s : snapshot.getChildren()) {
                            User user = s.getValue(User.class);
                            if (user.getEmail().equals(selectedUser.getEmail())) {
                                List<Integer> listOfInvites = user.getInvites().getInvites();
                                listOfInvites.add(Integer.parseInt(idCurrentUserLogged));
                                user.getInvites().setInvites(listOfInvites);
                                databaseReference.child(s.getKey()).setValue(user);
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });


    }

    /**
     * Connect to FirebaseStorage and get photo with id from logged user
     * @param user
     */
    private void setSelectedUserPhoto(User user) {
        firebaseStorage = FirebaseStorage.getInstance();
        if (user.getProfilePhotoID() != null) {
            storageReference = firebaseStorage.getReference().child("images/" + user.getProfilePhotoID());
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(selectedProfileImage);
                }
            });
        }
    }

    private void setSelectedUserDetails(User user) {
        fullName = findViewById(R.id.tvSelectedUserFullName);
        fullName.setText(user.getName() + " " + user.getSurName() + ", " + user.getAge());
        phoneNumber = findViewById(R.id.tvSelectedUserPhoneNumber);
        phoneNumber.setText(String.valueOf(user.getPhoneNumber()));
        email = findViewById(R.id.tvEmailSelectedUser);
        email.setText(user.getEmail());
    }



}