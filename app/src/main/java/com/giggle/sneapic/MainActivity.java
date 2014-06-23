package com.giggle.sneapic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.giggle.sneapic.config.PeepConfig;
import com.giggle.sneapic.service.PeepServiceScheduler;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WakefulIntentService.scheduleAlarms(new PeepServiceScheduler(), this, false);

        // initialize UI
        initializeUI();
    }

    private void initializeUI() {
        final PeepConfig config = SneapicApplication.getPeepConfig();

        final ToggleButton picTakenSwitch = (ToggleButton) findViewById(R.id.auto_take_switch);
        picTakenSwitch.setChecked(config.isEnableAutoTakePic());

        picTakenSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean on = ((ToggleButton) v).isChecked();
                config.setEnableAutoTakePic(on);
            }
        });
    }
}
