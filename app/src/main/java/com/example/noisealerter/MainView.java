package com.example.noisealerter;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;

public class MainView extends SurfaceView implements Runnable {
    private boolean run;
    private Thread thread;
    private double maxVolume;
    private final int THRESHOLD;
    private MediaRecorder recorder;
    private MainActivity activity;
    private int screenHeight, screenWidth;
    private Button homeButton;
    private Paint outlinePaint, fillPaint, textPaint;
    private ArrayList<AlerterData> fileNames;
    private int checkVolIdx;
    private boolean isMonitoring;
    private String filePath, fileName;
    private boolean finishRecord;
    private double maxVolumeWithinRec;
    private int downloadIdx;
    private ArrayList<ListItem> listItems;
    private MediaPlayer player;
    private boolean startPlaying;
    private Bitmap playbutton, pausebutton;


    public MainView(Context context, Resources res) {
        super(context);
        run = true;

        isMonitoring = false;

        finishRecord = false;
        startPlaying = false;

        maxVolume = 0;
        maxVolumeWithinRec = 0;

        THRESHOLD = 88;

        checkVolIdx = 0;
        downloadIdx = 0;

        activity = (MainActivity) getActivity();
        screenHeight = activity.SCREENHEIGHT;
        screenWidth = activity.SCREENWIDTH;

        filePath = activity.filePath;
        fileName = activity.filePath + "/audiorecordtest.3gp";


        homeButton = new Button(screenWidth/20, screenHeight/30, screenWidth, screenHeight, "Away");

        outlinePaint = new Paint();
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(5);
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(80);
        fillPaint = new Paint();

        fileNames = new ArrayList<AlerterData>(0);
        listItems = new ArrayList<ListItem>(0);

        playbutton = BitmapFactory.decodeResource(getResources(), R.drawable.playbutton);
        playbutton = Bitmap.createScaledBitmap(playbutton, screenWidth/10, screenWidth/10, false);
        pausebutton = BitmapFactory.decodeResource(getResources(), R.drawable.pausebutton);
        pausebutton = Bitmap.createScaledBitmap(pausebutton, screenWidth/10, screenWidth/10, false);
    }



    public void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = this.getHolder().lockCanvas();
            canvas.drawARGB(255, 255, 255, 255);
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(120);

            //draw home button
            textPaint.setColor(Color.WHITE);
            homeButton.draw(canvas, screenWidth/18, outlinePaint, textPaint);

            //write volume
            textPaint.setColor(Color.BLACK);
            String volumeString = String.format("%1$,.1f", maxVolume);
            canvas.drawText("Volume: " + volumeString + " dB", (int) (screenWidth/2.5), screenHeight/12, textPaint);

            //draw list
            if (fileNames.size() <= 5) {
                for (int i = 0; i < fileNames.size(); i++) {
                    listItems.get(listItems.size() - i - 1).draw(canvas, screenHeight / 7 * (i + 1), playbutton, pausebutton);
                }
            } else {
                int listIdx = fileNames.size() - 1;
                for (int i = 0; i < 5; i++) {
                    listItems.get(listIdx).draw(canvas, screenHeight / 7 * (i + 1), playbutton, pausebutton);
                    listIdx--;
                }
            }

            getHolder().unlockCanvasAndPost(canvas);

        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        recorder.setMaxDuration(10000);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            public void onInfo(MediaRecorder arg0, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    if (maxVolumeWithinRec > THRESHOLD) {
                        stopRecording();
                        isMonitoring = false;
                        onUpload();
                    }
                    isMonitoring = false;
                }
            }
        });

        try  {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("volume", "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecording() {
        if (isMonitoring) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private void getVolume() {
        if (isMonitoring) {
            int volume = recorder.getMaxAmplitude();
            if (volume > 0) {
                maxVolume = 20 * (float) (Math.log10(volume));
                if (maxVolume > maxVolumeWithinRec) {
                    maxVolumeWithinRec = maxVolume;
                }
            }
        }
    }

    public void onUpload() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        StorageReference audioFile = storageRef.child("audio/" + currentDateAndTime);

        String copyFileName = filePath + "/" + currentDateAndTime + ".3gp";
        copyFile(fileName, copyFileName);

        UploadTask uploadTask = audioFile.putFile(Uri.fromFile(new File(copyFileName)));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("upload", "upload failed :(");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("upload", "UPLOAD SUCCEED");
                isMonitoring = true;
            }
        });
    }

    private boolean existsAlready(String name) {
        name = name.substring(7);
        for (int i = 0; i < fileNames.size(); i++) {
            String currName = fileNames.get(i).getDateAndTime();
            Log.d("download", "testing " + name + " " + currName);
            if (name.equals(currName)) {
                Log.d("download", "ALREADY EXISTS");
                return true;
            }
        }
        return false;
    }

    public void getList() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child("audio");

        listRef.listAll()
            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    for (StorageReference prefix : listResult.getItems()) {
                        String name = prefix.getPath();

                        if (!existsAlready(name)) {
                            downloadFile(name);
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("download", "ondownload failed :(((");
                }
            });

    }

    private void downloadFile (String path) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference fileRef = storage.getReference().child(path);

        File localFile = null;
        try {
            localFile =  File.createTempFile("audio", ".3gp");
            Log.d("download", "final filename " + filePath + path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileName = localFile.getPath();
        AlerterData tempData = new AlerterData(path.substring(7), fileName);
        fileNames.add(tempData);
        listItems.add(new ListItem(tempData, screenWidth, screenHeight));
        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("download", "DOWNLOAD SUCCESS");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@Nonnull Exception exception) {
                Log.d("download", "download failed :(");
            }
        });
    }

    private void copyFile(String srcName, String dstName)
    {
        try {
            File src = new File(srcName);
            File dst = new File(dstName);


            if (!dst.exists()) {
                dst.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try {
                source = new FileInputStream(src).getChannel();
                destination = new FileOutputStream(dst).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (run) {
            if (homeButton.isHome() && !isMonitoring) {
                if (recorder != null) {
                    stopRecording();
                }
                maxVolumeWithinRec = 0;
                startRecording();
                isMonitoring = true;
            }
            if (checkVolIdx < 10) {
                checkVolIdx++;
            } else {
                if (isMonitoring) {
                    getVolume();
                    checkVolIdx = 0;
                }
            }
            if (downloadIdx < 500) {
                downloadIdx++;
            } else {
                getList();
                downloadIdx = 0;
            }

            draw();
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {
        thread = new Thread(this);
        thread.start();
    }

    public void pause () {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    private void startPlaying(String playName, int i) {
        player = new MediaPlayer();
        try {
            player.setDataSource(playName);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    listItems.get(i).setIsPlaying(false);
                }
            });
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("play", "prepare() failed");
        }
    }

    public boolean onPressEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                if (homeButton.isClicked(x, y)) {
                    homeButton.clickButton();
                }
                for (int i = 0; i < listItems.size(); i++) {
                    if (listItems.get(i).isClicked(x, y, playbutton)) {
                        startPlaying(listItems.get(i).getAlerterData().getLocalFilePath(), i);
                    }
                }
                break;
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean clicked = onPressEvent(event);
        return true;
    }
}
