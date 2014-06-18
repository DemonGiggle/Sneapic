package com.giggle.sneapic.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * Created by giggle on 2014/6/18.
 */
public class PeepServiceScheduler implements WakefulIntentService.AlarmListener {
    private static final long FREQUENCY = 30 * 1000;

    @Override
    public void scheduleAlarms(AlarmManager alarmManager, PendingIntent pendingIntent, Context context) {

        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FREQUENCY,
                FREQUENCY, pendingIntent);
    }

    @Override
    public void sendWakefulWork(Context context) {
        WakefulIntentService.sendWakefulWork(context, PeepService.class);
    }

    @Override
    public long getMaxAge() {
        return FREQUENCY;
    }
}
