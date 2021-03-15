package com.example.messanger.Chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.messanger.R;


public class MessageAdapter extends ArrayAdapter<String> {

    private Activity context;
    private String[] messages;

    public MessageAdapter(Activity context, String[] messages) {
        super(context, R.layout.messages_list, messages);
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.messages_list, null, true);
        TextView message = rowView.findViewById(R.id.mess);
        message.setText(messages[position]);

        return rowView;
    }
}
