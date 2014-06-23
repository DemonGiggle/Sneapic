package com.giggle.sneapic.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by giggle on 6/23/14.
 */
public class PeepConfig {
    private static final String CONFIG_NAME = "good_life";

    private static final String KEY_AUTO_TAKE_PIC = "auto_take_pic";

    private SharedPreferences setting;
    private SharedPreferences.Editor settingEditor;

    private boolean enableAutoTakePic;

    public PeepConfig(Context context) {
        setting = context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        settingEditor = setting.edit();

        enableAutoTakePic = setting.getBoolean(KEY_AUTO_TAKE_PIC, true);
    }

    public boolean isEnableAutoTakePic() {
        return enableAutoTakePic;
    }

    public void setEnableAutoTakePic(boolean enableAutoTakePic) {
        this.enableAutoTakePic = enableAutoTakePic;
        settingEditor.putBoolean(KEY_AUTO_TAKE_PIC, enableAutoTakePic);
        settingEditor.apply();
    }
}
