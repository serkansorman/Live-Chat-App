package com.example.serkansorman.chat;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Base64;

public class AESencryption{

    /**
     * Singleton patterni uygulanması için kullanılacak referans
     */
    private static AESencryption AESencrypt;
    /**
     * AES alogoritması için kullanılacak rastgele belirlenmiş key
     */
    private String key;

    public AESencryption(Context context) {

        key = context.getString(R.string.AES_KEY);
    }

    public static AESencryption getInstance(Context context){
        if(AESencrypt == null)
            AESencrypt = new AESencryption(context);

        return AESencrypt;
    }


    /**
     * AES algoritması kullanılarak mesaj şifrelenir.
     *
     * @param data mesaj
     * @return mesajın şifrelenmiş hali
     */
    public String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        return Base64.encodeToString(encVal,Base64.DEFAULT);
    }

    /**
     * AES algoritması kullanılarak şifrelenmiş mesaj çözülür.
     *
     * @param encryptedData şifrelenmiş mesaj
     * @return mesajın şifresinin çözülmüş hali.
     */
    public String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(encryptedData,Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);

    }


    /**
     * Şifrelemede kullanmak üzere yeni bir key üretir.
     */
    private Key generateKey() throws UnsupportedEncodingException {
        return new SecretKeySpec(key.getBytes("UTF-8"), "AES");
    }



}
