package com.example.serkansorman.chat;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.PowerManager;

import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class DeviceControl {

    private Context context;

    DeviceControl(Context context){
        this.context = context;
    }


    /**
     * Kilitli durumda olan ekranı açar.
     */
    public void openScreen(){

        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);

        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
        wl.acquire(10000);
        PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
        wl_cpu.acquire(10000);
    }

    /**
     * Ekranın kilitli olup olmadığı kontrol edilir.
     * @return telefon ekranı kilitliyse true return edilir.
     */
    public boolean isScreenLocked(){
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }


    /**
     * Bildirim göndermek için uygulamanın ekranda açık olup olmadığı kontrol edilir.
     * @return uygulama ekranda acıksa true return edilir
     */
    public boolean isApplicationOnForeground() {

        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return topActivity.getPackageName().equals(context.getPackageName());

        }

        return false;
    }
}
