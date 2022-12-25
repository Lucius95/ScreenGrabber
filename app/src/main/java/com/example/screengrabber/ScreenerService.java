package com.example.screengrabber;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.CursorJoiner;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.Session2Command;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.hardware.display.VirtualDisplay;
import android.net.Uri;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.view.Display;
import android.widget.Toast;
import android.util.Log;
import java.lang.Object;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.FileOutputStream;

public class ScreenerService extends Service implements ImageReader.OnImageAvailableListener
{
    private int _count;
    private ImageReader _imageReader;
    MediaProjectionManager _mediaProjectionManager;
    MediaProjection _mediaProjection;
    VirtualDisplay _virtualDisplay;

    private final String CHANNEL_ID = "ScreenerServiceNotification";

    //TODO: Жека в своем проекте: сделал эту переменую volatile
    private boolean _isStarted;

    @Override
    public void onCreate() {
        Log.i(ScreenerService.class.getName(), "CreateScreenerService");
        Toast.makeText(this, "CreateScreenerService", Toast.LENGTH_LONG);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CreateBitmapsBytesExecutor();
            }
        };

        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(_isStarted) return START_NOT_STICKY;

        _isStarted = true;

        //TODO: Жека в своем проекте:
        //Указывать что тукущий поток должен иметь статус высокого приоритета

        Intent data = (Intent)intent.getParcelableExtra("MEDIA_PROJECTION_DATA");
        String ip = intent.getStringExtra("IP");
        int width = intent.getIntExtra("WIDTH_PIXELS", 0);
        int height = intent.getIntExtra("HEIGHT_PIXELS", 0);
        int dpi = intent.getIntExtra("DENSITY_DPI", 0);

        //TODO: Жека в своем проекте:
        //Создает экземпляр клиента, для общения с сервером

        StartForegroundForService();

        _imageReader = ImageReader.newInstance(width, height, (int)0x00000001, 60);
        _imageReader.setOnImageAvailableListener(this, null);

         _mediaProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
         _mediaProjection = _mediaProjectionManager.getMediaProjection((int)-1, data);
        _virtualDisplay = _mediaProjection.createVirtualDisplay("ScreenCapture", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, _imageReader.getSurface(), null, null);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();

        if(image != null)
        {
            _count++;
            SaveImage(image, "Image" + _count + ".png");
            image.close();
        }
    }

    public void SaveImage(Image image, String fileName){
        try{
            Image.Plane plane = image.getPlanes()[0];
            Bitmap bitmap = Bitmap.createBitmap(plane.getRowStride() / plane.getPixelStride(), plane.getBuffer().remaining() / plane.getRowStride(), Bitmap.Config.ARGB_8888);
            FileOutputStream out = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception ex){

        }
    }

    public static void SaveToTheGalley(String filePath, Context context)
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
    }


    public void CreateBitmapsBytesExecutor(){

        while(true)
        {

        }
    }

    protected void StartForegroundForService(){
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "ScreenerService", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ScreenerServiceTitle")
                .setContentText("ScreenerServiceText")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(2, notification);
    }
}
