package com.giggle.sneapic.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.easycamera.DefaultEasyCamera;
import com.easycamera.EasyCamera;
import com.giggle.sneapic.MainActivity;
import com.giggle.sneapic.R;
import com.giggle.sneapic.SneapicApplication;

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
    private SurfaceView surface;

    private final Lock lock = new ReentrantLock();
    private Condition picTake = lock.newCondition();

    private volatile boolean isPhotoTaken = false;

    private WindowManager windowManager;
    private NotificationManager notificationManager;

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

            // Display notification icon
            final Intent activity = new Intent(getApplicationContext(), MainActivity.class);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setContentTitle(getString(R.string.notification_last_taken_title))
                   .setContentText(getString(R.string.notification_last_taken_content) + formatter.format(System.currentTimeMillis()))
                   .setSmallIcon(R.drawable.app_icon)
                   .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, activity, PendingIntent.FLAG_UPDATE_CURRENT));
            notificationManager.notify(R.string.tag_notificaiton_last_taken, builder.build());

            isPhotoTaken = true;
            try {
                picTake.signal();
            } catch (IllegalMonitorStateException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void doWakefulWork(Intent intent) {
        lock.lock();

        // Wait for photo taken
        Log.d(TAG, "Check is photo taken");
        while (!isPhotoTaken) {
            try {
                picTake.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalMonitorStateException e) {
                e.printStackTrace();
            } finally {

                try {
                    lock.unlock();
                } catch (IllegalMonitorStateException e) {
                    e.printStackTrace();
                }
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

        if (!SneapicApplication.getPeepConfig().isEnableAutoTakePic()) {
            Log.d(TAG, "auto take pic config is disabled, just leave");
            isPhotoTaken = true;
            return;
        }

        // create folder for
        photoOutputFolder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SneaPic");

        if (!photoOutputFolder.exists()) {
            photoOutputFolder.mkdir();
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                10, 10,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        surface = new SurfaceView(this);
        surface.setZOrderOnTop(true);
        surface.getHolder().setFormat(PixelFormat.TRANSPARENT);

        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surface created");

                // initialize picture size
                final Camera.Parameters parameters = camera.getParameters();
                final Camera.Size size = getLargestSize(parameters);
                parameters.setPictureSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(0);

                // start to take picture
                try {
                    EasyCamera.CameraActions actions = camera.startPreview(surface.getHolder());
                    actions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(pictureCallback));
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surface destroyed");
            }
        });

        windowManager.addView(surface, params);

        camera = DefaultEasyCamera.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (camera != null) {
            camera.close();
        }

        if (windowManager != null) {
            windowManager.removeView(surface);
        }
    }
}
