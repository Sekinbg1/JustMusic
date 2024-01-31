package ly.jj.newjustpiano.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ly.jj.newjustpiano.Keyboard;
import ly.jj.newjustpiano.R;
import ly.jj.newjustpiano.tools.DatabaseRW;
import ly.jj.newjustpiano.tools.SequenceExtractor;

import static ly.jj.newjustpiano.items.StaticItems.playingThread;
import static ly.jj.newjustpiano.items.StaticItems.soundMixer;

public class SongListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private Cursor cursor;

    public SongListAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        cursor.moveToPosition(i);
        if (view == null) {
            view = inflater.inflate(R.layout.song_panel, null);
            ((TextView) view.findViewById(R.id.song_name)).setText(cursor.getString(DatabaseRW.SONG_NAME) + "-" + cursor.getString(DatabaseRW.SONG_AUTHOR));
            view.findViewById(R.id.song_name).setSelected(true);
            byte[] song = cursor.getBlob(DatabaseRW.SONG_DATA);
            view.findViewById(R.id.song_play).setOnClickListener(v -> {
                Intent intent = new Intent(context, Keyboard.class);
                intent.putExtra("song", song);
                context.startActivity(intent);
            });
            view.findViewById(R.id.song_listen).setOnClickListener(v -> {
                SequenceExtractor sequenceExtractor = new SequenceExtractor(song);
                sequenceExtractor.extractor();
                sequenceExtractor.setOnNextListener((value, volume) -> soundMixer.play(value, volume));
                if (playingThread != null) {
                    playingThread.interrupt();
                    playingThread = null;
                }
                playingThread = new Thread(sequenceExtractor::sequence);
                playingThread.start();
            });
            view.findViewById(R.id.song_share).setOnClickListener(v -> {

            });
        }
        return view;
    }
}
