package com.giggle.sneapic.service;

import android.content.Intent;
import android.hardware.Camera;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by giggle on 2014/6/18.
 */
public class PeepService extends WakefulIntentService {
    private static final String TAG = "PeepService";

    private File photoOutputFolder;

    private EasyCamera camera;
    private SurfaceHolder surface;

    private final Lock lock = new ReentrantLock();
    private Condition picTake = lock.newCondition();
    private volatile boolean isPhotoTaken;

    public PeepService() {
        super("PeepService");
    }

    EasyCamera.PictureCallback pictureCallback = new EasyCamera.PictureCallback() {
        public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
            Log.d(TAG, "Picture taken, size = " + data.length);

            //final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
            final String filename = formatter.format(System.currentTimeMillis());

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(new File(photoOutputFolder, filename + ".jpg"));
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            isPhotoTaken = true;
            try {
                picTake.signal();
            } catch (IllegalMonitorStateException e) {
                Log.d(TAG, e.toString());
            }
        }
    };

    @Override
    protected void doWakefulWork(Intent intent) {
        isPhotoTaken = false;
        lock.lock();

        // initialize picture size
        final Camera.Parameters parameters = camera.getParameters();
        final Camera.Size size = getLargestSize(parameters);
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(0);

        // start to take picture
        try {
            EasyCamera.CameraActions actions = camera.startPreview(surface);
            actions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(pictureCallback));
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        // Wait for photo taken
        Log.d(TAG, "Check is photo taken");
        while (!isPhotoTaken) {
            try {
                picTake.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        Log.d(TAG, "service finished");
    }

    private Camera.Size getLargestSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (final Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            }
            else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service launched");
        // create folder for
        photoOutputFolder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SneaPic");

        if (!photoOutputFolder.exists()) {
            photoOutputFolder.mkdir();
        }

        SurfaceView view = new SurfaceView(this);
        surface = view.getHolder();
        camera = DefaultEasyCamera.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        camera.close();
    }
}
