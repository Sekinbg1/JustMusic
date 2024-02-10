package ly.jj.newjustpiano;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import androidx.annotation.Nullable;
import ly.jj.newjustpiano.tools.Sequence;
import ly.jj.newjustpiano.tools.SequenceExtractor;
import ly.jj.newjustpiano.views.BarrageView;
import ly.jj.newjustpiano.views.KeyboardView;

import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.tools.StaticTools.setFullScreen;

public class Keyboard extends ly.jj.newjustpiano.Activity {
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setFullScreen(getWindow().getDecorView());
        //setNoNotchBar(getWindow());
        setContentView(R.layout.keyboard);
        BarrageView barrageView = findViewById(R.id.barrage_view);
        KeyboardView keyboardView = findViewById(R.id.keyboard_view);

        barrageView.setFreshRate(90);
        keyboardView.setOnKeyDownListener(barrageView::onkeyboard);
        SequenceExtractor sequenceExtractor = new SequenceExtractor(getIntent().getByteArrayExtra("song"));
        sequenceExtractor.extractor();
        sequenceExtractor.setOnNextListener((barrageView::addKey));
        if (playingThread != null) {
            playingThread.interrupt();
            playingThread = null;
        }
        playingThread = new Thread(sequenceExtractor::sequence);
        playingThread.start();
        Button play = findViewById(R.id.testplay);
        play.setText("auto!");
        play.setOnClickListener(view -> {
            barrageView.setAutoPlay(true);
        });
        Button add = findViewById(R.id.testadd);
        add.setText("add!");
        add.setOnClickListener(e -> {
            barrageView.addCount(1);
            keyboardView.addCount(1);
        });
        Button reduce = findViewById(R.id.testreduce);
        reduce.setText("reduce!");
        reduce.setOnClickListener(e -> {
            barrageView.addCount(-1);
            keyboardView.addCount(-1);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playingThread.interrupt();
        playingThread = null;
    }
}
