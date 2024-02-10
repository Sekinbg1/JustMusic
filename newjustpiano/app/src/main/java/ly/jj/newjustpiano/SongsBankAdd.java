package ly.jj.newjustpiano;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;

import java.util.List;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;
import static ly.jj.newjustpiano.items.StaticItems.database;
import static ly.jj.newjustpiano.tools.StaticTools.testaccounts;

public class SongsBankAdd extends ly.jj.newjustpiano.Activity {
    private EditText name, creator, info;

    ImageView pri_help, online_help;
    private Switch pri, online;
    private TextView pri_text;
    private Spinner account;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_bank_add);
        name = findViewById(R.id.songs_bank_add_name);
        creator = findViewById(R.id.songs_bank_add_creator);
        pri_help = findViewById(R.id.songs_bank_add_public_help);
        online_help = findViewById(R.id.songs_bank_add_online_help);
        pri_text = findViewById(R.id.songs_bank_add_public_text);
        pri = findViewById(R.id.songs_bank_add_public);
        online = findViewById(R.id.songs_bank_add_online);
        account = findViewById(R.id.songs_bank_add_account);
        info = findViewById(R.id.songs_bank_add_info);
        ArrayAdapter<String> simpleAdapter = new ArrayAdapter<>(this, R.layout.songs_bank_add_spinner_adapter, testaccounts);
        account.setAdapter(simpleAdapter);
        online.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                pri_help.setVisibility(View.VISIBLE);
                pri_text.setVisibility(View.VISIBLE);
                pri.setVisibility(View.VISIBLE);
                account.setVisibility(View.VISIBLE);
            } else {
                pri_help.setVisibility(View.INVISIBLE);
                pri_text.setVisibility(View.INVISIBLE);
                pri.setVisibility(View.INVISIBLE);
                account.setVisibility(View.INVISIBLE);
            }
        });
        findViewById(R.id.songs_bank_add_conform).setOnClickListener(view -> {
            String nameText = name.getText().toString();
            String creatorText = creator.getText().toString();
            String infoText = info.getText().toString();
            String accountText = ((TextView) account.getSelectedView()).getText().toString();
            boolean isPublic = pri.isChecked();
            boolean isOnline = online.isChecked();
            if (nameText.length() < 3) {
                return;
            }
            if (creatorText.length() < 2) {
                return;
            }
            database.addSongsBank(nameText, creatorText, isPublic, isOnline, isOnline ? accountText : null, infoText);
            this.finish();
        });
        findViewById(R.id.songs_bank_add_cancel).setOnClickListener(view ->
                this.finish());
    }
}
