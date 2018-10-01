package com.example.serkansorman.chat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;


public class MessagesActivity extends Activity implements IMessagesActivity{

    private ArrayList<Message> messagesList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private final int PICK_SPEECH_INPUT = 113;
    private ImageLoader imageLoader;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_screen);

        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        String roomName = prefs.getString("roomName", "MainRoom");//"MainRoom" is the default room.
        userName = prefs.getString("userName", "Anonymous");//"Anonymous" is the default value.

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("ChatRooms").child(roomName).child("Messages");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                LinearLayout allMessages = findViewById(R.id.allMessages);

                for(DataSnapshot data : dataSnapshot.getChildren()) {

                    final Message message = data.getValue(Message.class);

                    //Tüm mesajlar ekranda gösterilir.
                    if(!messagesList.contains(message)){
                        messagesList.add(message);

                        if(message.getId().startsWith("-")) // Image message
                            showImageMessages(message,allMessages);
                        else
                            showTextMessages(message,allMessages);
                    }
                }
                //En son mesajın gösterilmesi için sayfanın en altına kaydırılır.
                scrollDown(0);
                scrollDown(800);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MessagesActivity","The read failed: " + databaseError.getCode());
            }
        });

        //Giriş yapılan oda ismi mesaj sayfasının üstüne set edilir.
        TextView textView = findViewById(R.id.roomName);
        textView.setText(roomName);
    }

    private void showImageMessages(final Message message,LinearLayout allMessages){

        View view = getLayoutInflater().inflate(R.layout.image_message,null);

        final TextView nameImage = view.findViewById(R.id.userNameImage);
        final ImageView imageView = view.findViewById(R.id.image);
        final TextView dateImage = view.findViewById(R.id.timeImage);

        nameImage.setText(message.getUserName());
        dateImage.setText(message.getTime());
        Picasso.with(getApplicationContext()).load(message.getText()).fit().centerCrop().into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveImageToGallery(imageView);

                Intent intent = new Intent(MessagesActivity.this, FullScreenImage.class);
                intent.putExtra("url", message.getText());
                startActivity(intent);

            }
        });
        allMessages.addView(view);
    }

    private void showTextMessages(final Message message, LinearLayout allMessages){

        View view = getLayoutInflater().inflate(R.layout.text_message, null);
        final TextView messageText = view.findViewById(R.id.Text);

        if(message.getUserName().equals(userName))
            messageText.setBackgroundResource(R.drawable.own_message_body);

        final TextView name = view.findViewById(R.id.userName);
        final TextView date = view.findViewById(R.id.time);

        try {
            messageText.setText(AESencryption.getInstance(this).decrypt(message.getText()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        name.setText(message.getUserName());
        date.setText(message.getTime());

        allMessages.addView(view);

    }

    public void sendMessage(View view) throws Exception {

        //Her mesaj için unique id üretilir
        Random random = new Random();
        Long id = random.nextLong();

        //Sadece text içeren mesajlara pozitif id verilir.
        id *= (id<0) ? -1:1;

        //Databaseye gönderilecek mesaj şifrelenir.
        EditText editText = findViewById(R.id.editMessage);
        String encryptedMessage = AESencryption.getInstance(this).encrypt(editText.getText().toString());

        if(!editText.getText().toString().isEmpty()){
            Message message = new Message(encryptedMessage,userName,id.toString());
            databaseReference.push().setValue(message);
            editText.getText().clear();
        }

    }

    private void sendImageUrl(String url){
        //Her mesaj için unique id üretilir
        Random random = new Random();
        Long id = random.nextLong();

        //Image içeren mesajlara negatif id verilir.
        id *= (id>0) ? -1:1;

        Message message = new Message(url,userName,id.toString());
        databaseReference.push().setValue(message);
    }



    public void chooseImage(View view) {

        imageLoader = new ImageLoader(MessagesActivity.this);
        final Dialog customDialog = new Dialog(this);
        customDialog.setContentView( R.layout.custom_alert_dialog);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.getWindow().setGravity(Gravity.BOTTOM);

        Button camera = customDialog.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                imageLoader.takePhotoFromCamera();
            }
        });

        Button gallery = customDialog.findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                imageLoader.loadImageFromGallery();
            }
        });
        customDialog.show();
    }

    public void startVoiceInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me your message");
        try {
            startActivityForResult(intent, PICK_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech Recognition is not sported",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImage(Uri filePath){

        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Image Sending...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            Bitmap bmp = decodeBitmap(getApplicationContext(),filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            UploadTask uploadTask = ref.putBytes(baos.toByteArray());

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    sendImageUrl(taskSnapshot.getDownloadUrl().toString());
                    Toast.makeText(MessagesActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();
                    }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MessagesActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Sending... "+(int)progress+"%");
                        }
                    });
        }
    }

    private void saveImageToGallery(ImageView imageView){

        imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = imageView.getDrawingCache();
        MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"image","MyChat");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == imageLoader.getGALLERY_IMG_REQUEST() && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            Uri filePath = data.getData();
            uploadImage(filePath);

        }
        else if(requestCode == imageLoader.getCAMERA_IMG_REQUEST() && resultCode == RESULT_OK){

            File imgFile = new  File(imageLoader.getPictureFilePath());
            if(imgFile.exists())            {
                uploadImage(Uri.fromFile(imgFile));
            }

        }
        else if(requestCode == PICK_SPEECH_INPUT && resultCode == RESULT_OK){

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            EditText editText = findViewById(R.id.editMessage);
            editText.setText(result.get(0));

        }
    }


    private Bitmap decodeBitmap(Context context, Uri theUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
    }


    private void scrollDown(int delayTime){

        final ScrollView sv = findViewById(R.id.scroll);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(View.FOCUS_DOWN);
            }
        }, delayTime);
    }

}
