package com.example.tinder;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ArrayAdapter extends android.widget.ArrayAdapter<Cards> {
    private Context context;

    public ArrayAdapter(Context context, int resourceId, List<Cards> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Cards cardItem = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item,parent,false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        name.setText(cardItem.getName());
        //no_cards_placehorder.setImageResource(R.mipmap.ic_launcher);
        if (cardItem.getProfileImageUrl()==null){
            image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(getContext()).load(cardItem.getProfileImageUrl()).into(image);
        }

        return convertView;
    }
}
