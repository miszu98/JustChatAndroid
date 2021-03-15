package com.example.messanger.MainSite;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
 * Created by Michal Malek
 * Own implements Array adapter to display List View
 */
public class InviteAdapter extends ArrayAdapter<String> {

    private Activity context;
    private String[] titles;
    private String[] images;
    private String[] mails;

    public InviteAdapter(Activity context, String[] titles, String[] images, String[] mails) {
        super(context, R.layout.invites_list, titles);
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
     * Button to accept invite after click connect to database get data about logged user
     * update friends list - add and invites list - remove
     * Button to decline invite after click connect to database get data about logged user
     * update invite list - remove
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.invites_list, null, true);

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


        Button btnAccept = rowView.findViewById(R.id.btnAcceptFriend);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBase dataBase = new DataBase();
                DatabaseReference databaseReference = dataBase.firebase.getReference().child("Users");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.R)
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
                        List<Integer> invites = user.getInvites().getInvites();
                        for (List<Serializable> x : users) {
                            User userToAdd = (User) x.get(1);
                            if (userToAdd.getEmail().equals(mails[position])) {
                                friends.add(Integer.parseInt(String.valueOf(x.get(0))));
                                invites.remove((Integer) Integer.parseInt(String.valueOf(x.get(0))));
                                user.getInvites().setInvites(invites);
                                user.getFriends().setListFriends(friends);

                                DatabaseReference reference = dataBase.firebase.getReference().child("Users");
                                reference.child(key).setValue(user);

                                List<Integer> friendsOtherUser = userToAdd.getFriends().getListFriends();
                                friendsOtherUser.add(Integer.parseInt(key));
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

        Button btnDecline = rowView.findViewById(R.id.btnDeclinetFriend);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBase dataBase = new DataBase();
                DatabaseReference databaseReference = dataBase.firebase.getReference().child("Users");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.R)
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
                        List<Integer> invites = user.getInvites().getInvites();
                        for (List<Serializable> x : users) {
                            User userToAdd = (User) x.get(1);
                            if (userToAdd.getEmail().equals(mails[position])) {
                                invites.remove((Integer) Integer.parseInt(String.valueOf(x.get(0))));
                                user.getInvites().setInvites(invites);
                                DatabaseReference reference = dataBase.firebase.getReference().child("Users");
                                reference.child(String.valueOf(key)).setValue(user);
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
