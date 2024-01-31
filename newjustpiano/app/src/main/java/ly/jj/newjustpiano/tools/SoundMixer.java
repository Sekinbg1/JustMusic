package ly.jj.newjustpiano.tools;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class SoundMixer {
    public final static int AUDIO_MODE_LOW_LATENCY = 2;
    public final static int AUDIO_MODE_DEFAULT = 1;
    public final static int AUDIO_MODE_POWER_SAVE = 0;

    private final File[] sounds = new File[128];
    private final ReentrantLock lock = new ReentrantLock();

    @SuppressLint("SdCardPath")
    public SoundMixer() {
        Log.i("info", "now try golang lib");
        System.out.println(soundMixer.SoundMixer.init());
    }

    public SoundMixer(int sampleRate, int channels) {
        build(sampleRate, channels);
    }

    public void setSound(File path) {
        sounds[Integer.parseInt(path.getName())] = path;
        System.out.println("Loading audio " + path.getName());
        soundMixer.SoundMixer.loadSound(path.getName(), path.getPath());
        //soundMixer.SoundMixer.play(path.getName());
    }

    public void build(int sampleRate) {
        this.build(sampleRate, 2);
    }

    public void build(int sampleRate, int channels) {
    }


    public void play(File file) {
        if (file != null)
            play(file.getPath());
    }

    public void play(String path) {
        play(path, 255);
    }

    public void play(int i, int volume) {
        if (sounds[i] != null) {
            play(sounds[i].getName(), volume);
        }
    }


    public void play(String path, int volume) {
        soundMixer.SoundMixer.play(path, (float) ((volume * volume) / 32768.0));
    }

    public void setMode(int i) {
    }

}
