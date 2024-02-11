package ly.jj.newjustpiano.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import ly.jj.newjustpiano.R;
import ly.jj.newjustpiano.items.BarrageKey;
import ly.jj.newjustpiano.items.StaticItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;
import static ly.jj.newjustpiano.tools.StaticTools.zoomBitmap;

public class BarrageView extends View {
    private final Paint paint = new Paint();
    private final List<BarrageKey> drawKeys = new CopyOnWriteArrayList<>();
    private final List<BarrageKey> playKeys = new CopyOnWriteArrayList<>();
    private int keyCount;
    private final ReentrantLock lock = new ReentrantLock();
    private String source = "0";

    private String delta = "";
    private int sourceI = 0;

    private int combo = 0;
    private Bitmap note_white;
    private Bitmap note_black;
    private Bitmap note_play;
    private Bitmap note_prac;
    private float interval;
    private float barrageWidth;
    private float barrageHeight;
    private int viewWidth;
    private int viewHeight;
    private int sleep_ms;
    private int sleep_ns;
    private float step;
    private boolean autoPlay;
    private final Thread iterator = new Thread(() -> {
        new Thread(() -> {
            try {
                while (true) {
                    sleep(sleep_ms, sleep_ns);
                    invalidate();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            while (true) {
                lock.lock();
                List<BarrageKey> list = new ArrayList<>();
                for (BarrageKey key : drawKeys) {
                    key.addTime(step);
                    if (key.length > viewHeight + barrageHeight + barrageHeight / 2) {
                        list.add(key);
                        doLevel(3);
                    }
                }
                if (autoPlay) {
                    for (BarrageKey key : list) {
                        StaticItems.soundMixer.play(key.value, key.volume);
                    }
                }
                drawKeys.removeAll(list);
                list.clear();
                for (BarrageKey key : playKeys) {
                    key.addTime(step);
                    if (key.length > viewHeight + barrageHeight / 2) list.add(key);
                }
                playKeys.removeAll(list);
                for (BarrageKey key : list) {
                    StaticItems.soundMixer.play(key.value, key.volume);
                }
                lock.unlock();
                sleep(2);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
    private int drawCount;

    public BarrageView(Context context) {
        super(context);
        keyCount = 8;
        paint.setColor(Color.BLACK);
        paint.setTextSize(80);
        iterator.start();
    }

    public BarrageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.KeyboardView, 0, 0);
        keyCount = a.getInt(R.styleable.KeyboardView_Count, 8);
        a.recycle();
        paint.setColor(Color.BLACK);
        paint.setTextSize(80);
        iterator.start();
    }

    public BarrageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.KeyboardView, 0, 0);
        keyCount = a.getInt(R.styleable.KeyboardView_Count, 8);
        a.recycle();
        paint.setColor(Color.BLACK);
        paint.setTextSize(80);
        iterator.start();
    }

    public void setFreshRate(int rate) {
        sleep_ms = 1000 / rate;
        sleep_ns = 1000000000 / rate - sleep_ms * 1000000;
    }

    public void setCount(int i) {
        keyCount = i;
        resize();
        invalidate();
    }

    public void addCount(int i) {
        keyCount += i;
        resize();
        invalidate();
    }

    public void addKey(int value, int volume) {
        lock.lock();
        if (drawKeys.size() == 0 || drawKeys.get(drawKeys.size() - 1).length > 60)
            drawKeys.add(new BarrageKey(0, volume, value, 0));
        else playKeys.add(new BarrageKey(0, volume, value, 0));
        lock.unlock();
    }

    private void doLevel(int level) {
        if (level == 0) {
            addsource(combo > 14 ? 20 : 5 + combo);
            combo++;
        } else if (level == 1) {
            addsource(combo > 11 ? 15 : 4 + combo);
            //combo = 0;
        } else if (level == 2) {
            addsource(2);
            combo = 0;
        } else if (level == 3) {
            //addsource(-1);
        } else if (level == 4) {
            addsource(-5);
            combo = 0;
        }
    }

    private void addsource(int s) {
        delta = Integer.toString(s);
        sourceI += s;
        if (sourceI < 0) sourceI = 0;
        source = Integer.toString(sourceI);
    }

    private int abs(int a) {
        if (a < 0) return -a;
        else return a;
    }

    public void onkeyboard(int value) {
        boolean played = false;
        for (BarrageKey key : drawKeys) {
            if (key.value % drawCount == value) {
                if (viewHeight - key.length - 4 * barrageHeight < 0) {
                    int delta = abs((int) (viewHeight - key.length + barrageHeight / 2));
                    drawKeys.remove(key);
                    StaticItems.soundMixer.play(key.value, key.volume);
                    played = true;
                    if (delta < barrageHeight / 2) {
                        doLevel(0);
                    } else if (delta < barrageHeight) {
                        doLevel(1);
                    } else if (delta < 2 * barrageHeight) {
                        doLevel(2);
                    } else if (delta < 3 * barrageHeight) {
                        doLevel(3);
                    } else {
                        doLevel(4);
                    }
                    break;
                } else {
                    StaticItems.soundMixer.play(key.value, key.volume);
                    played = true;
                    doLevel(4);
                }

            }
        }
        if (!played) {
            doLevel(4);
            if (drawKeys.isEmpty())
                StaticItems.soundMixer.play(value + 12 * 4, 0x7f);
            else
                StaticItems.soundMixer.play((drawKeys.get(0).value / drawCount) * drawCount + value, drawKeys.get(0).volume);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    private void resize() {
        int r = keyCount / 7;
        int l = keyCount % 7;
        drawCount = r * 12 + (l > 1 ? (l > 2 ? (l > 4 ? (l > 5 ? l + 4 : l + 3) : l + 2) : l + 1) : l);
        interval = (float) viewWidth / keyCount / 2;

        barrageWidth = (float) viewWidth / keyCount * 2 / 5;
        barrageHeight = barrageWidth * 3 / 5;

        note_white = zoomBitmap(StaticItems.note_white, barrageWidth, barrageHeight);
        note_black = zoomBitmap(StaticItems.note_black, barrageWidth, barrageHeight);
        note_play = zoomBitmap(StaticItems.note_play, barrageWidth, barrageHeight);
        note_prac = zoomBitmap(StaticItems.note_prac, barrageWidth, barrageHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        boolean first = true;
        int play, r, l, value;
        for (BarrageKey key : drawKeys) {
            play = key.value % drawCount;
            r = play / 12;
            l = play % 12;
            l = (l > 4 ? l + 1 : l) + 1;
            value = r * 14 + l;
            if (first) {
                first = false;
                canvas.drawBitmap(note_play, value * interval - barrageWidth / 2, (key.length - barrageHeight * 3 / 2), paint);
                continue;
            }
            canvas.drawBitmap((value % 2) == 0 ? note_black : note_white, value * interval - barrageWidth / 2, (key.length - barrageHeight * 3 / 2), paint);
        }
        canvas.drawText(source, viewWidth - 80 * source.length(), 80, paint);

        canvas.drawText(delta, viewWidth / 2 - 40 * source.length(), 240, paint);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        step = viewHeight / 300;
        resize();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidate();
    }
}
