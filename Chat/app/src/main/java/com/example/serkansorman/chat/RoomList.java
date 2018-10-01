package com.example.serkansorman.chat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RoomList extends Activity{

    private RoomAdapter roomAdapter;
    private ArrayList<Room> rooms = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_list);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("ChatRooms");
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Message message;
                for (DataSnapshot room : dataSnapshot.getChildren()){
                    ArrayList<String> users = new ArrayList<>();
                    for (DataSnapshot messageSnapShot : room.child("Messages").getChildren()){
                        message = messageSnapShot.getValue(Message.class);
                        if(!users.contains(message.getUserName()))
                            users.add(message.getUserName());
                    }

                    if( ! rooms.contains(new Room(room.getKey(),users.size())))
                        rooms.add(new Room(room.getKey(),users.size()));

                }

                ListView listView = findViewById(R.id.room_list);
                roomAdapter = new RoomAdapter(getApplicationContext(),R.layout.room_info,rooms);
                listView.setAdapter(roomAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        ConstraintLayout constraintLayout = (ConstraintLayout) view;
                        TextView textView = constraintLayout.findViewById(R.id.roomName);

                        SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
                        editor.putString("roomName",textView.getText().toString());
                        editor.apply();

                        startService(new Intent(new Intent(getApplicationContext(),FireBaseBackgroundService.class)));
                        startActivity(new Intent(getApplicationContext(),MessagesActivity.class));

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    
    public void addNewRoom(View view){
        EditText editText = findViewById(R.id.editRoomName);

        if (!editText.getText().toString().isEmpty() && ! rooms.contains(editText.getText().toString())){
            rooms.add( new Room(editText.getText().toString(),0));
            roomAdapter.notifyDataSetChanged();
        }
    }

}
