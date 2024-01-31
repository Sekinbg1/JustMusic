package ly.jj.newjustpiano;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;

public class EmptyActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
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
