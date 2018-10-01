package com.example.serkansorman.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


public class RoomAdapter extends ArrayAdapter<Room> {

    private ArrayList<Room> roomList;

    RoomAdapter(Context context, int textViewResourceId, ArrayList<Room> rooms) {
        super(context, textViewResourceId, rooms);
        roomList = rooms;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.room_info, null);
        TextView textView = view.findViewById(R.id.roomName);
        TextView textView2 = view.findViewById(R.id.numOfUser);
        textView.setText(roomList.get(position).getName());
        textView2.setText((roomList.get(position)).getNumOfUser().toString());

        return view;

    }

}