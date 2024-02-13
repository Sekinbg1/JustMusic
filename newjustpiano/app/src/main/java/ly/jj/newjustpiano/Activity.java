package ly.jj.newjustpiano;

import android.os.Bundle;
import androidx.annotation.Nullable;
import ly.jj.newjustpiano.tools.StaticTools;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;

public class Activity extends android.app.Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StaticTools.context = this;
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
