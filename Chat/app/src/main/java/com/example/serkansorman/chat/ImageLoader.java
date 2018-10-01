package com.example.serkansorman.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageLoader{

    private String  pictureFilePath;
    private final int GALLERY_IMG_REQUEST = 71;
    private final int CAMERA_IMG_REQUEST  = 79;
    private Context context;

    public ImageLoader(Context context){
        this.context = context;
    }


    public String getPictureFilePath() {
        return pictureFilePath;
    }

    public int getCAMERA_IMG_REQUEST() {
        return CAMERA_IMG_REQUEST;
    }

    public int getGALLERY_IMG_REQUEST() {
        return GALLERY_IMG_REQUEST;
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "MyChat_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }


    public void loadImageFromGallery(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_IMG_REQUEST);
    }

    public void takePhotoFromCamera(){

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFile;
        try {
            pictureFile = getPictureFile();
        } catch (IOException ex) {
            Log.e("MessagesActivity","Image file can not be created");
            return;
        }
        if (pictureFile != null) {
            Uri photoURI = FileProvider.getUriForFile(context,
                    "fileprovider", pictureFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            ((Activity) context).startActivityForResult(cameraIntent, CAMERA_IMG_REQUEST);
        }
    }
}
