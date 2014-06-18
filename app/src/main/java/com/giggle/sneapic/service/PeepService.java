package com.giggle.sneapic.service;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.easycamera.DefaultEasyCamera;
import com.easycamera.EasyCamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by giggle on 2014/6/18.
 */
public class PeepService extends WakefulIntentService {
    private static final String TAG = "PeepService";

    private File photoOuputFolder;

    private EasyCamera camera;
    private SurfaceHolder surface;

    public PeepService() {
        super("PeepService");
    }

    EasyCamera.PictureCallback pictureCallback = new EasyCamera.PictureCallback() {
        public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
            Log.d(TAG, "Picture taken, size = " + data.length);
            camera.close();

            //final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
            final String filename = formatter.format(System.currentTimeMillis());

            try {
                final FileOutputStream fos = new FileOutputStream(new File(photoOuputFolder, filename + ".jpg"));
                fos.write(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.d(TAG, "service finished");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service launched");
        // create folder for
        photoOuputFolder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SneaPic");
        if (!photoOuputFolder.exists()) {
            photoOuputFolder.mkdir();
        }

        SurfaceView view = new SurfaceView(this);
        surface = view.getHolder();
        camera = DefaultEasyCamera.open();

        try {
            EasyCamera.CameraActions actions = camera.startPreview(surface);
            actions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(pictureCallback));
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }
}
