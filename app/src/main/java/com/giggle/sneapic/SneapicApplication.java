package com.giggle.sneapic;

import android.app.Application;

import com.giggle.sneapic.config.PeepConfig;

/**
 * Created by giggle on 6/23/14.
 */
public class SneapicApplication extends Application {
    private static PeepConfig peepConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        peepConfig = new PeepConfig(this);
    }

    public static PeepConfig getPeepConfig() {
        return peepConfig;
    }
}
