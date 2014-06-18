package com.giggle.sneapic.service;

import android.content.Intent;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.easycamera.DefaultEasyCamera;
import com.easycamera.EasyCamera;

import java.io.IOException;

/**
 * Created by giggle on 2014/6/18.
 */
public class PeepService extends WakefulIntentService {
    private static final String TAG = "PeepService";

    private SurfaceHolder surface;

    public PeepService() {
        super("PeepService");
    }

    EasyCamera.PictureCallback pictureCallback = new EasyCamera.PictureCallback() {
        public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
            Log.d(TAG, "Picture taken, size = " + data.length);
        }
    };

    @Override
    protected void doWakefulWork(Intent intent) {
        EasyCamera camera = DefaultEasyCamera.open();
        try {
            EasyCamera.CameraActions actions = camera.startPreview(surface);
            actions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(pictureCallback));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SurfaceView view = new SurfaceView(this);
        surface = view.getHolder();
    }
}
