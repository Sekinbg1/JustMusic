package ly.jj.newjustpiano;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;
import ly.jj.newjustpiano.Adapter.SettingListAdapter;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;

public class Setting extends ly.jj.newjustpiano.Activity {
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.setting_page);
        TableLayout table = findViewById(R.id.setting_table);
        new SettingListAdapter(this, table).start();
    }
}
