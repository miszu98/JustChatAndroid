package com.example.messanger.MainSite;

import android.app.Activity;
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

public class ConversationAdapter extends ArrayAdapter<String> {

    private Activity context;
    private String[] images;
    private String[] names;
    private String[] lastMessage;


    public ConversationAdapter(Activity context, String[] images, String[] names, String[] lastMessage) {
        super(context, R.layout.conversations_list, names);
        this.context = context;
        this.images = images;
        this.names = names;
        this.lastMessage = lastMessage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.conversations_list, null, true);

        TextView fullname = rowView.findViewById(R.id.tvConvFullName);
        ImageView imageView = rowView.findViewById(R.id.cvPhotoConv);
        TextView lastMess = rowView.findViewById(R.id.tvLastMessage);

        fullname.setText(names[position]);
        lastMess.setText(lastMessage[position]);

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
