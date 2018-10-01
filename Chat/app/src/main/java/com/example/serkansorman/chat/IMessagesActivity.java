package com.example.serkansorman.chat;

import android.view.View;

/**
 * Mesaj ekranında bulunan mesaj yollama,resim ekleme ve sesli mesajı yazıya dönüştürme
 * butonlarının yapacağı işlevleri belirten methodları içerir.
 */
public interface IMessagesActivity {

    void sendMessage(View view) throws Exception;
    void chooseImage(View view);
    void startVoiceInput(View view);
}
