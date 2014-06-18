package com.giggle.sneapic;

import android.app.Activity;
import android.os.Bundle;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.giggle.sneapic.service.PeepServiceScheduler;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WakefulIntentService.scheduleAlarms(new PeepServiceScheduler(), this, false);
    }
}
