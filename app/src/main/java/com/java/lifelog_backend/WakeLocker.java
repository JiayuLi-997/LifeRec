package com.java.lifelog_backend;

import android.content.Context;
import android.os.PowerManager;

public abstract class WakeLocker {
    private static PowerManager.WakeLock wakeLock;

    //永久锁
    public static void acquire(Context ctx) {
        if (wakeLock != null) wakeLock.release();
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE | PowerManager.ACQUIRE_CAUSES_WAKEUP, "package:lifelog_backend");
        wakeLock.acquire();
    }

    //超时锁
    public static void acquire(Context ctx, int mSecond) {
        if (wakeLock != null) wakeLock.release();
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE | PowerManager.ACQUIRE_CAUSES_WAKEUP, "package:lifelog_backend");
        wakeLock.acquire(mSecond);
    }

    public static void release() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        wakeLock = null;
    }
}
