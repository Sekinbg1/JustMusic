package ly.jj.newjustpiano;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.Adapter.SongBankListAdapter;
import ly.jj.newjustpiano.views.ListBoxGirdview;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;
import static ly.jj.newjustpiano.items.StaticItems.database;

public class Songs extends Activity {
    private ListBoxGirdview list;
    private SongBankListAdapter adapter;
    private EditText search;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.songs);
        list = findViewById(R.id.songs_find_list);
        search = findViewById(R.id.songs_find_song_name);
        View find = findViewById(R.id.songs_find_song);
        adapter = new SongBankListAdapter(this, database.getBanks());
        find.setOnClickListener(v -> {
            JSONObject[] cursor = database.getBanks(search.getText().toString());
            adapter.setCursor(cursor);
            list.setAdapter(adapter);
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                JSONObject[] cursor = database.getBanks(editable.toString());
                adapter.setCursor(cursor);
                list.setAdapter(adapter);
            }
        });
        list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JSONObject[] cursor = database.getBanks(search.getText().toString());
        adapter.setCursor(cursor);
        list.setAdapter(adapter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        ActivityCount--;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityCount++;
    }
}
