package ly.jj.newjustpiano.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.R;
import ly.jj.newjustpiano.tools.DatabaseRW;
import ly.jj.newjustpiano.tools.SequenceExtractor;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static ly.jj.newjustpiano.items.StaticItems.*;

public class OnlineSongBankListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private JSONObject bankInfo;
    private JSONArray cursor;
    private int Height;
    private int colNum;

    public OnlineSongBankListAdapter(Context context, JSONArray cursor, int colNum) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.colNum = colNum;
        this.cursor = cursor;
    }

    public void setCursor(JSONArray cursor) {
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return colNum * ((cursor.size() - 1) / colNum + 1);
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
        if (cursor.size() > i) {
            JSONObject object = cursor.getJSONObject(i);
            String name = object.getString("name");
            String info = object.getString("info");
            String creator = object.getString("creator");
            String onlineAccount = object.getString("onlineAccount");
            if (view == null) {
                view = inflater.inflate(R.layout.song_bank_panel, null);
                ((TextView) view.findViewById(R.id.song_bank_name)).setText(name);
                ((TextView) view.findViewById(R.id.song_bank_info)).setText(creator + ":" + (info.length() > 0 ? info : "这个曲库没有更多说明"));
                View finalView = view;
                finalView.findViewById(R.id.song_bank_delete).setVisibility(INVISIBLE);
                view.findViewById(R.id.song_bank_info_list).setVisibility(INVISIBLE);
                View finalView1 = view;
                view.findViewById(R.id.song_bank_list_button).setOnClickListener(view1 -> {
                    if (finalView.findViewById(R.id.song_bank_info_text).getVisibility() == VISIBLE) {
                        finalView.findViewById(R.id.song_bank_info_text).setVisibility(INVISIBLE);
                        finalView.findViewById(R.id.song_bank_info_list).setVisibility(VISIBLE);
                        Cursor sl = database.readByKey("bank", name);
                        String[] l = new String[sl.getCount()];
                        for (int j = 0; j < sl.getCount(); j++) {
                            sl.moveToPosition(j);
                            l[j] = sl.getString(DatabaseRW.SONG_NAME);
                        }
                        ((ListView) finalView1.findViewById(R.id.song_bank_list)).setAdapter(new ArrayAdapter(finalView1.getContext(),
                                R.layout.online_songs_bank_songlist_adapter, R.id.online_songs_songlist_adapter_text, l) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                convertView = super.getView(position, convertView, parent);
                                convertView.findViewById(R.id.online_songs_songlist_adapter_music).setOnClickListener(view -> {
                                    byte[] song = database.getSong(name, getItem(position).toString());
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
                                return convertView;
                            }
                        });
                    } else {
                        finalView.findViewById(R.id.song_bank_info_list).setVisibility(INVISIBLE);
                        finalView.findViewById(R.id.song_bank_info_text).setVisibility(VISIBLE);
                    }
                });
                view.findViewById(R.id.song_bank_add).setVisibility(INVISIBLE);
                if (i == 0) {
                    Height = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight() - 100;
                }
                view.setLayoutParams(new ViewGroup.LayoutParams(-1, Height));
            }
        } else {
            if (view == null) {
                view = inflater.inflate(R.layout.songs_bank_songlist_adapter, null);
            }
            view.setLayoutParams(new ViewGroup.LayoutParams(-1, Height));
        }
        return view;
    }
}