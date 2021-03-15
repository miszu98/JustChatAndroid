package com.example.messanger.MainSite;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.messanger.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


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
        return rowView;
    }
}
