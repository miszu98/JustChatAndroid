package com.example.messanger.MainSite;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.messanger.Models.User;
import com.example.messanger.R;
import com.example.messanger.Repositories.DataBase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created By Michal Malek
 * Own implement Array adapter to display formatted ListView
 */
public class FriendAdapter extends ArrayAdapter<String> {

    private Activity context;
    private String[] titles;
    private String[] images;
    private String[] mails;


    public FriendAdapter(Activity context, String[] titles, String[] images, String[] mails) {
        super(context, R.layout.friend_list, titles);
        this.context = context;
        this.titles = titles;
        this.images = images;
        this.mails = mails;
    }

    /**
     *
     * By this param we can get data from 3 arrays (titles, images, mails)  @param position
     * for example index 0 in all arrays give one record data, info about one user
     * Take access to friend_list.xml view and set values to controls on this view
     * Images loads from FirebaseStorage via Glide
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.friend_list, null, true);

        TextView titleText = rowView.findViewById(R.id.title);
        ImageView imageView = rowView.findViewById(R.id.icon);
        TextView mailText = rowView.findViewById(R.id.subtitle);

        titleText.setText(titles[position]);
        mailText.setText(mails[position]);

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("images/" + images[position]);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(imageView);
            }
        });

        Button btnDelete = rowView.findViewById(R.id.deleteFriend);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBase dataBase = new DataBase();
                DatabaseReference databaseReference = dataBase.firebase.getReference().child("Users");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String authenticator = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        String key = null;
                        User user = null;
                        List<List<Serializable>> users = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            User u = s.getValue(User.class);
                            if (u.getEmail().equals(authenticator)) {
                                key = s.getKey();
                                user = u;
                            } else {
                                users.add(Arrays.asList(s.getKey(), u));
                            }
                        }
                        List<Integer> friends = user.getFriends().getListFriends();
                        for (List<Serializable> x : users) {
                            User userToAdd = (User) x.get(1);
                            if (userToAdd.getEmail().equals(mails[position])) {
                                friends.remove(Integer.parseInt(String.valueOf(x.get(0))));
                                user.getFriends().setListFriends(friends);

                                DatabaseReference reference = dataBase.firebase.getReference().child("Users");
                                reference.child(key).setValue(user);

                                List<Integer> friendsOtherUser = userToAdd.getFriends().getListFriends();
                                friendsOtherUser.remove(Integer.parseInt(key));
                                userToAdd.getFriends().setListFriends(friendsOtherUser);
                                reference.child(String.valueOf(x.get(0))).setValue(userToAdd);
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



        return rowView;
    }
}
