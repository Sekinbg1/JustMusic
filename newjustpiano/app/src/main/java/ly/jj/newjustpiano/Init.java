package ly.jj.newjustpiano;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ProgressBar;
import androidx.core.app.ActivityCompat;
import go.Seq;
import ly.jj.newjustpiano.tools.DatabaseRW;
import ly.jj.newjustpiano.tools.MediaDecoder;
import ly.jj.newjustpiano.tools.SoundMixer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.tools.SoundMixer.AUDIO_MODE_DEFAULT;
import static ly.jj.newjustpiano.tools.StaticTools.setFullScreen;

public class Init extends Activity {

    ProgressBar progress;

    private void verifyStorage() {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if ((checkSelfPermission(PERMISSIONS[0]) | checkSelfPermission(PERMISSIONS[1])) != PackageManager.PERMISSION_GRANTED)
                ;
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_EXTERNAL_STORAGE);
    }


    @SuppressLint("SdCardPath")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setFullScreen(getWindow().getDecorView());
        //setNoNotchBar(getWindow());
        setContentView(R.layout.init);
        progress = findViewById(R.id.init_progress);
        verifyStorage();
        new Thread(() -> {
            progress.setProgress(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = getDataDir();
            } else {
                data = getDir("data", MODE_PRIVATE);
            }
            database = new DatabaseRW(openOrCreateDatabase("settings.db", MODE_PRIVATE, null), openOrCreateDatabase("songs.db", MODE_PRIVATE, null));
            freshRate = getWindowManager().getDefaultDisplay().getRefreshRate();
            try {
                white_m = BitmapFactory.decodeStream(getAssets().open("keyboard/white_m.png"));
                white_r = BitmapFactory.decodeStream(getAssets().open("keyboard/white_r.png"));
                white_l = BitmapFactory.decodeStream(getAssets().open("keyboard/white_l.png"));
                black = BitmapFactory.decodeStream(getAssets().open("keyboard/black.png"));

                white_m_p = BitmapFactory.decodeStream(getAssets().open("keyboard/white_m_p.png"));
                white_r_p = BitmapFactory.decodeStream(getAssets().open("keyboard/white_r_p.png"));
                white_l_p = BitmapFactory.decodeStream(getAssets().open("keyboard/white_l_p.png"));
                black_p = BitmapFactory.decodeStream(getAssets().open("keyboard/black_p.png"));

                note_white = BitmapFactory.decodeStream(getAssets().open("keyboard/white_note.png"));
                note_black = BitmapFactory.decodeStream(getAssets().open("keyboard/black_note.png"));
                note_play = BitmapFactory.decodeStream(getAssets().open("keyboard/play_note.png"));
                note_prac = BitmapFactory.decodeStream(getAssets().open("keyboard/prac_note.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Seq.setContext(getApplicationContext());
            soundMixer = new SoundMixer();
            cache = getCacheDir();
            sounds = new File(cache, "sounds");
            if (!sounds.exists()) sounds.mkdirs();
            try {
                MediaDecoder decoder = new MediaDecoder(30);
                String[] soundStr = getAssets().list("sounds");
                for (int i = soundStr.length - 1; i >= 0; i--) {
                    if (!soundStr[i].endsWith("ogg")) continue;
                    File outSound = new File(sounds, soundStr[i].substring(0, soundStr[i].indexOf('.')));
                    soundMixer.setSound(outSound);
                    if (outSound.exists()) continue;
                    AssetFileDescriptor afd = getAssets().openFd("sounds/" + soundStr[i]);
                    decoder.set(afd);
                    decoder.decode();
                    progress.setProgress(i * 100 / soundStr.length);
                    new FileOutputStream(outSound).write(decoder.read());
                }
                if (audioFormat == null) {
                    AssetFileDescriptor afd = getAssets().openFd("sounds/" + soundStr[0]);
                    decoder.set(afd);
                    decoder.decode();
                }
                progress.setProgress(100);
                decoder.release();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.gc();
                soundMixer.setMode(AUDIO_MODE_DEFAULT);
                soundMixer.build(sampleRate(), channelCount());
            }
            Intent intent = new Intent(this, Main.class);
            intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }).start();
    }
}
