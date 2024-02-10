package ly.jj.newjustpiano;

import static ly.jj.newjustpiano.items.StaticItems.ActivityCount;

public class Activity extends android.app.Activity {
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
