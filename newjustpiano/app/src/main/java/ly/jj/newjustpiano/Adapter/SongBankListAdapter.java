package ly.jj.newjustpiano.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.EmptyActivity;
import ly.jj.newjustpiano.R;
import ly.jj.newjustpiano.SongsBankAdd;
import ly.jj.newjustpiano.SongsBankSongAdd;
import ly.jj.newjustpiano.tools.DatabaseRW;

import java.util.Arrays;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static ly.jj.newjustpiano.items.StaticItems.database;
import static ly.jj.newjustpiano.tools.StaticTools.testaccounts;

public class SongBankListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private JSONObject[] cursor;
    private int Height;

    public SongBankListAdapter(Context context, JSONObject[] cursor) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.cursor = cursor;
    }

    public void setCursor(JSONObject[] cursor) {
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return 5 * (cursor.length / 5 + 1);
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
        if (i == 0) {
            if (view == null) {
                view = inflater.inflate(R.layout.song_bank_panel, null);
                ((TextView) view.findViewById(R.id.song_bank_name)).setText("新建曲库");
                ((TextView) view.findViewById(R.id.song_bank_info)).setTextSize(100);
                ((TextView) view.findViewById(R.id.song_bank_info)).setText("+");
                ((TextView) view.findViewById(R.id.song_bank_info)).setGravity(Gravity.CENTER);
                view.findViewById(R.id.song_bank_list_button).setVisibility(INVISIBLE);
                view.findViewById(R.id.song_bank_list).setVisibility(INVISIBLE);
                view.findViewById(R.id.song_bank_delete).setVisibility(INVISIBLE);
                view.findViewById(R.id.song_bank_info_list).setVisibility(INVISIBLE);
                view.findViewById(R.id.song_bank_info).setOnClickListener(view1 -> {
                    Intent intent = new Intent(context, SongsBankAdd.class);
                    context.startActivity(intent);
                });
            }
            Height = view.getMeasuredHeight();
        } else {
            if (cursor.length > (i - 1)) {
                JSONObject object = cursor[i - 1];
                String name = object.getString("name");
                String info = object.getString("info");
                String creator = object.getString("creator");
                String online = object.getString("online");
                String onlineAccount = object.getString("onlineAccount");
                String pub = object.getString("public");
                if (view == null) {
                    view = inflater.inflate(R.layout.song_bank_panel, null);
                    ((TextView) view.findViewById(R.id.song_bank_name)).setText(name);
                    ((TextView) view.findViewById(R.id.song_bank_info)).setText(creator + ":" + (info.length() > 0 ? info : "这个曲库没有更多说明"));

                    View finalView = view;
                    finalView.findViewById(R.id.song_bank_delete).setOnClickListener(view1 -> {
                        AlertDialog.Builder b = new AlertDialog.Builder(context, R.style.NoDiaBG);
                        View v2 = inflater.inflate(R.layout.songs_bank_delete_conform, null);
                        b.setView(v2);
                        AlertDialog a = b.show();
                        v2.findViewById(R.id.alert_button_cancel).setOnClickListener(v3 -> {
                            a.dismiss();
                        });
                        v2.findViewById(R.id.alert_button_conform).setOnClickListener(v3 -> {
                            database.deleteBank(name);
                            a.dismiss();
                            Intent intent = new Intent(context, EmptyActivity.class);
                            context.startActivity(intent);
                        });
                    });
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
                                l[j] = sl.getString(DatabaseRW.SONG_NAME) + "-" + sl.getString(DatabaseRW.SONG_AUTHOR);
                            }
                            ((ListView) finalView1.findViewById(R.id.song_bank_list)).setAdapter(new ArrayAdapter(finalView1.getContext(),
                                    R.layout.songs_bank_songlist_adapter, l));
                        } else {
                            finalView.findViewById(R.id.song_bank_info_list).setVisibility(INVISIBLE);
                            finalView.findViewById(R.id.song_bank_info_text).setVisibility(VISIBLE);
                        }
                    });
                    if (online.equals("false") | Arrays.asList(testaccounts).contains(onlineAccount)) {
                        view.findViewById(R.id.song_bank_add).setOnClickListener(view1 -> {
                            Intent intent = new Intent(context, SongsBankSongAdd.class);
                            intent.putExtra("name", name);
                            intent.putExtra("online", online);
                            intent.putExtra("public", pub);
                            intent.putExtra("account", onlineAccount);
                            context.startActivity(intent);
                        });
                    } else {
                        view.findViewById(R.id.song_bank_add).setVisibility(INVISIBLE);
                    }
                    view.setLayoutParams(new ViewGroup.LayoutParams(-1, Height));
                }
            } else {
                if (view == null) {
                    view = inflater.inflate(R.layout.songs_bank_songlist_adapter, null);
                    view.setLayoutParams(new ViewGroup.LayoutParams(-1, Height));
                }
            }
        }
        return view;
    }
}
