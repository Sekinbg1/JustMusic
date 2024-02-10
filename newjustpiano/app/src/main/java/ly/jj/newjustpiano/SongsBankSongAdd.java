package ly.jj.newjustpiano;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;

import java.io.*;
import java.util.Arrays;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;
import static ly.jj.newjustpiano.items.StaticItems.database;
import static ly.jj.newjustpiano.tools.StaticTools.testaccounts;

public class SongsBankSongAdd extends ly.jj.newjustpiano.Activity {
    TextView bank;
    EditText name;
    EditText file;
    EditText author;
    Spinner account;
    TextView textView;
    Uri stream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_bank_add_song);
        bank = findViewById(R.id.songs_bank_add_song_bank);
        name = findViewById(R.id.songs_bank_add_song_name);
        file = findViewById(R.id.songs_bank_add_song_file);
        author = findViewById(R.id.songs_bank_add_song_author);
        account = findViewById(R.id.songs_bank_add_song_account);
        textView = findViewById(R.id.songs_bank_add_song_text);
        bank.setText(getIntent().getStringExtra("name"));
        if (getIntent().getStringExtra("online").equals("true")) {
            String a = getIntent().getStringExtra("account");
            textView.setVisibility(View.VISIBLE);
            account.setVisibility(View.VISIBLE);
            if (getIntent().getStringExtra("public").equals("true")) {
                ArrayAdapter<String> simpleAdapter = new ArrayAdapter<>(this, R.layout.songs_bank_add_spinner_adapter, testaccounts);
                account.setAdapter(simpleAdapter);
            } else {
                if (Arrays.asList(testaccounts).contains(a)) {
                    ArrayAdapter<String> simpleAdapter = new ArrayAdapter<>(this, R.layout.songs_bank_add_spinner_adapter, new String[]{a});
                    account.setAdapter(simpleAdapter);
                } else {
                    this.finish();
                }
            }
        }
        findViewById(R.id.songs_bank_add_song_cancel).setOnClickListener(view -> SongsBankSongAdd.this.finish());
        findViewById(R.id.songs_bank_add_song_conform).setOnClickListener(view -> {
            try {
                BufferedInputStream reader = new BufferedInputStream(getBaseContext().getContentResolver().openInputStream(stream));
                int size = reader.available();
                byte[] data = new byte[size];
                int readsize = reader.read(data);
                database.addSong(name.getText().toString(), bank.getText().toString(), author.getText().toString(),
                        getIntent().getStringExtra("online").equals("true") ? ((TextView) account.getSelectedView()).getText().toString() : "", data);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            SongsBankSongAdd.this.finish();
        });
        findViewById(R.id.songs_bank_add_song_select).setOnClickListener(view -> {
            //调用系统文件管理器打开指定路径目录
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //intent.setDataAndType(Uri.fromFile(new File("/storage/0/")), "audio/midi");
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 0);
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stream = data.getData();
        CursorLoader loader = new CursorLoader(this, stream, new String[]{"_display_name", "artist", "author"}, null, null, null);
        Cursor cursor = loader.loadInBackground();
        cursor.moveToFirst();
        String name = cursor.getString(0);
        String artist = cursor.getString(1);
        String author = cursor.getString(2);
        cursor.close();
        file.setText(name.substring(0, name.lastIndexOf(".")));
        if (artist != null && !artist.isEmpty()) {
            this.author.setText(artist);
        } else if (author != null && !author.isEmpty()) {
            this.author.setText(author);
        } else if (name.contains("-")) {
            this.author.setText(name.substring(name.indexOf("-"), name.lastIndexOf(".")));
        }
        if (name.contains("-"))
            this.name.setText(name.substring(0, name.lastIndexOf(".")).substring(0, name.indexOf("-")));
        else
            this.name.setText(name.substring(0, name.lastIndexOf(".")));
    }
}
