package ly.jj.newjustpiano.tools;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.alibaba.fastjson2.JSONObject;

public class DatabaseRW {
    private final SQLiteDatabase settings;
    private final SQLiteDatabase songs;

    public static int SONG_DATA;
    public static int SONG_ID;
    public static int SONG_NAME;
    public static int SONG_SUBREGION;
    public static int SONG_AUTHOR;
    public static int SONG_PAUTHOR;
    public static int SONG_SOURCE;
    public static int SONG_BANK;

    public DatabaseRW(SQLiteDatabase settings, SQLiteDatabase songs) {
        this.settings = settings;
        this.songs = songs;
        settings.execSQL("create table if not exists local(" +
                "set_name text not null primary key," +
                "value text not null)");
        songs.execSQL("create table if not exists songs(" +
                "id integer not null primary key autoincrement," +
                "name text not null," +
                "author text not null," +
                "pauthor text not null," +
                "source int default 0," +
                "bank text not null," +
                "song blob not null)");
        songs.execSQL("create table if not exists banks(" +
                "id integer not null primary key autoincrement," +
                "creator text not null," +
                "public bool not null," +
                "online bool not null," +
                "onlineAccount text," +
                "info text," +
                "name text not null)");
        String[] columnNames = songs.query("songs", new String[]{"*"}, null, null, null, null, null).getColumnNames();
        for (int i = 0; i < columnNames.length; i++) {
            switch (columnNames[i]) {
                case "song":
                    SONG_DATA = i;
                    break;
                case "id":
                    SONG_ID = i;
                    break;
                case "name":
                    SONG_NAME = i;
                    break;
                case "subregion":
                    SONG_SUBREGION = i;
                    break;
                case "author":
                    SONG_AUTHOR = i;
                    break;
                case "pauthor":
                    SONG_PAUTHOR = i;
                    break;
                case "source":
                    SONG_SOURCE = i;
                    break;
                case "bank":
                    SONG_BANK = i;
                    break;
            }
        }
        test();
    }

    public void writeSetting(String key, int value) {
        if (readSetting(key) != -1) {
            settings.execSQL("update local set value='" + value + "' where set_name like '" + key + "'");
        } else {
            settings.execSQL("insert into local(set_name,value) values('" + key + "','" + value + "')");
        }
    }

    public int readSetting(String key) {
        @SuppressLint("Recycle")
        Cursor cursor = settings.query("local", new String[]{"set_name", "value"}, "set_name like '" + key + "'", null, null, null, null);
        if (cursor.moveToNext()) {
            return cursor.getInt(1);
        }
        return -1;
    }

    public void test() {

        //songs.delete("songs", "id", new String[]{});
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('1','测试1','1','1','default','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('2','测试1','1','1','default','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('3','测试1','1','1','default','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('4','测试1','1','1','default','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('1','测试2','1','1','default2','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('1','测试3','1','1','default2','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('1','测试4','1','1','default1','" + data + "')");
        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('1','测试5','1','1','default1','" + data + "')");
    }

    public Cursor readSelects() {
        return songs.query("songs", new String[]{"bank"}, "", null, "bank", null, null);
    }

    public void addSongsBank(String name, String creator, boolean pri, boolean online, String onlineAccount, String info) {
        try {

            songs.execSQL("insert into banks(name,creator,public,online,onlineAccount,info) " +
                    "values('" + name + "','" + creator + "','" + pri + "','" + online + "','" + onlineAccount + "','" + info + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public byte[] getSong(String bank, String name) {
        Cursor cursor = songs.query("songs", new String[]{"song"}, "name=? and bank=?", new String[]{name, bank}, null, null, null);
        if (cursor.moveToNext())
            return cursor.getBlob(0);
        else return new byte[0];
    }

    public JSONObject[] getBanks() {
        Cursor cursor = songs.query("banks", new String[]{"name", "info", "creator", "online", "onlineAccount", "public"}, "name like ?", new String[]{"%"}, null, null, null);
        JSONObject[] jsonObjects = new JSONObject[cursor.getCount()];
        while (cursor.moveToNext()) {
            JSONObject object = new JSONObject();
            object.put("name", cursor.getString(0));
            object.put("info", cursor.getString(1));
            object.put("creator", cursor.getString(2));
            object.put("online", cursor.getString(3));
            object.put("onlineAccount", cursor.getString(4));
            object.put("public", cursor.getString(5));
            jsonObjects[cursor.getPosition()] = object;
        }
        return jsonObjects;
    }

    public void deleteBank(String name) {
        songs.delete("banks", "name=?", new String[]{name});
        songs.delete("songs", "bank=?", new String[]{name});
    }

    public JSONObject[] getBanks(String key) {
        Cursor cursor = songs.query("banks", new String[]{"name", "info", "creator", "online", "onlineAccount", "public"}, "name like ?", new String[]{"%"}, null, null, null);
        JSONObject[] jsonObjects = new JSONObject[cursor.getCount()];
        while (cursor.moveToNext()) {
            JSONObject object = new JSONObject();
            object.put("name", cursor.getString(0));
            object.put("info", cursor.getString(1));
            object.put("creator", cursor.getString(2));
            object.put("online", cursor.getString(3));
            object.put("onlineAccount", cursor.getString(4));
            object.put("public", cursor.getString(5));
            jsonObjects[cursor.getPosition()] = object;
        }
        return jsonObjects;
    }

    public void addSong(String name, String bank, String author, String pauthor, byte[] data) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("author", author);
        values.put("pauthor", pauthor);
        values.put("bank", bank);
        values.put("song", data);
        songs.insert("songs", null, values);

        //songs.execSQL("insert into songs(name,subregion,author,pauthor,bank,song) values('1','测试1','1','1','default','" + data + "')");
    }


    public Cursor readByKey(String key, String name) {
        return songs.query("songs", new String[]{"*"},
                key + "='" + name + "'",
                null, null, null, null);
    }
}
