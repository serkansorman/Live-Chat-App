package com.example.serkansorman.chat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

/**
 * Mesaj olarak yollanan resme tıklandığında resmi yeni bir ekranda gerçek boyutunda gösterir.
 */
public class FullScreenImage extends Activity {

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_screen);

        ImageView imageView = findViewById(R.id.fullImage);
        Picasso.with(this).load(getIntent().getStringExtra("url")).fit().centerCrop().into(imageView);

    }

}
