package ly.jj.newjustpiano;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;

public class EmptyActivity extends ly.jj.newjustpiano.Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
