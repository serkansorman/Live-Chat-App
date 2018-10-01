package com.example.serkansorman.chat;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uygulama kapalıyken telefona gelen mesaj bildirimlerini kontrol eden servis
 */
public class FireBaseBackgroundService extends Service {

    /**
     *  Giriş yapılan oda ismi
     */
    private String roomName;
    private DeviceControl deviceControl;

    @Override
    public void onStart(Intent intent, int startid) {

        Log.i("FireBaseService", "Message service started.");
        deviceControl = new DeviceControl(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        roomName = prefs.getString("roomName", "No name defined");

        DatabaseReference myRef = database.getReference("ChatRooms").child(roomName);
        myRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            }

            /**
             * Kullanıcının bulunduğu odada bir mesaj gönderildiğinde
             * bildirim gönderilir.
             * @param dataSnapshot
             * @param s
             */
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.i("FireBaseService", "New message received");

                ArrayList<Message> messagesList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    // Ekran kapalıysa bildirim geldiğinde 10 saniye ekranı açar
                    if (deviceControl.isScreenLocked())
                        deviceControl.openScreen();

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Message message = data.getValue(Message.class);
                        //Aynı mesajın birden fazla gösterilmesini engeller
                        if (!messagesList.contains(message))
                            messagesList.add(message);
                    }

                    //Son mesaj alınır.
                    Message lastMessage = messagesList.get(messagesList.size() - 1);

                    //Yeni mesaj geldiğinde uygulama ekranda açık değil ise bildirim gönderilir.
                    if (!deviceControl.isApplicationOnForeground()) {
                        try {
                            sendNotification(lastMessage);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError fireBaseError) {
            }
        });

    }

    /**
     * Kullanıcının bulunduğu rooma mesaj yazıldığında kullanıcıya bildirim gönderir.
     * @param lastMessage bildirim olarak gönderilecek son mesaj
     * @throws Exception
     */
    public void sendNotification(Message lastMessage) throws Exception {

        //Bildirim için titreşim,ses ve led rengi gibi ayarlar set edilir.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.one)
                        .setContentTitle(lastMessage.getUserName()+" - "+roomName)
                        .setVibrate(new long[] { 1000, 1000})
                        .setLights(Color.BLUE, 1000, 1000)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setAutoCancel(true);

        //Bildirim içinde mesaj içeriği gösterilir.
        if(lastMessage.getId().startsWith("-"))
            builder.setContentText("Image Message");
        else{
            builder.setContentText(AESencryption.getInstance(this).decrypt(lastMessage.getText()));
        }


        //Bildirime tıklandığında chat sayfasına geçilir.
        Intent intent=new Intent(getApplicationContext(),MessagesActivity.class);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(getApplicationContext());

        stackBuilder.addParentStack(MessagesActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent=stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0,builder.build());
    }


    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

}




