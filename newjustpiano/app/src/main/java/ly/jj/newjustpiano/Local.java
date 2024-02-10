package ly.jj.newjustpiano;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import ly.jj.newjustpiano.Adapter.SongListAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;

public class Local extends ly.jj.newjustpiano.Activity {
    private final List<Button> selectsList = new ArrayList<>();
    private LayoutInflater inflater;
    private Button stopPlay;
    private float textSize;

    @SuppressLint({"UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.local);
        inflater = LayoutInflater.from(this);
        textSize = new TextView(this).getTextSize() / getResources().getDisplayMetrics().density;
        LinearLayout flipper_select = findViewById(R.id.local_flipper_select);
        ViewFlipper flipper = findViewById(R.id.local_flipper);
        Cursor selects = database.readSelects();
        stopPlay = findViewById(R.id.local_playing_stop);
        stopPlay.setOnClickListener((View v) -> {
            if (playingThread != null) {
                playingThread.interrupt();
                playingThread = null;
            }
        });
        while (selects.moveToNext()) {
            Button button = new Button(this);
            button.setText(selects.getString(0));
            button.setTextSize(textSize);
            button.setBackground(getDrawable(R.drawable.button_background_t));
            button.setOnClickListener(v -> {
                int i = flipper.getDisplayedChild();
                int j = selectsList.indexOf(v);
                if (i == j) return;
                if (i > j) {
                    flipper.setInAnimation(Local.this, R.anim.slide_left_in);
                    flipper.setOutAnimation(Local.this, R.anim.slide_right_out);
                } else {
                    flipper.setInAnimation(Local.this, R.anim.slide_right_in);
                    flipper.setOutAnimation(Local.this, R.anim.slide_left_out);
                }
                flipper.setDisplayedChild(j);
                for (Button b : selectsList) {
                    b.setTextSize(textSize);
                }
                ((Button) v).setTextSize((float) (textSize * 1.5));
            });
            selectsList.add(button);
            flipper_select.addView(button);
        }
        if (selects.getCount() != 0)
            selectsList.get(0).setTextSize((float) (textSize * 1.5));
        setFlipperTouchListener(flipper, flipper, selectsList);
        selects.moveToFirst();
        do {
            GridView gridView = new GridView(this);
            gridView.setNumColumns(5);
            setFlipperTouchListener(gridView, flipper, selectsList);
            if (selects.getCount() != 0)
                gridView.setAdapter(new SongListAdapter(this, database.readByKey("bank", selects.getString(0))));
            flipper.addView(gridView);
        } while (selects.moveToNext());
        flipper.setDisplayedChild(0);
    }

    public static void setFlipperTouchListener(View v, ViewFlipper flipper, List<Button> selectsList) {
        v.setOnTouchListener(new View.OnTouchListener() {
            private float startX;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float moveX = 230f;
                float endX = motionEvent.getX();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = motionEvent.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (endX - startX > moveX) {
                            if (flipper.getDisplayedChild() == 0) return false;
                            selectsList.get(flipper.getDisplayedChild() - 1).callOnClick();
                            return true;
                        } else if (startX - endX > moveX) {
                            if (flipper.getDisplayedChild() + 1 == selectsList.size()) return false;
                            selectsList.get(flipper.getDisplayedChild() + 1).callOnClick();
                            return true;
                        }
                    case MotionEvent.ACTION_MOVE:
                        if (endX - startX > moveX || startX - endX > moveX) return true;
                }
                return false;
            }
        });
    }
}
