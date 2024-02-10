package ly.jj.newjustpiano.tools;

import Client.OnMessageListener;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.alibaba.fastjson2.JSONObject;

import static java.lang.Thread.sleep;
import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.items.StaticItems.SONG;

public class StaticTools {
    public static String[] testaccounts = {"test", "test1"};

    public static void setFullScreen(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(view);
            controller.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            view.setSystemUiVisibility(fullScreenFlags);
        }
    }

    public static JSONObject getSong(boolean online, String Class, String bank, String Song) {
        JSONObject object1 = new JSONObject();
        final JSONObject[] object2 = {null};
        object1.put("class", Class);
        object1.put("bank", bank);
        object1.put("song", Song);
        client.addOnMessageListener(SONG, new OnMessageListener() {
            @Override
            public void onEnd() {

            }

            @Override
            public void onMessage(byte[] bytes) {
                object2[0] = JSONObject.parseObject(new String(bytes));
            }
        });
        client.sendMessage(SONG, object1.toJSONString().getBytes());
        try {
            while (object2[0] == null) {
                sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return object2[0];
    }

    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();


        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;


        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public static void setNoNotchBar(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }

    public static Bitmap zoomBitmap(Bitmap source, float width, float height) {
        int x = source.getWidth();
        int y = source.getHeight();
        float scaleWidth = width / x;
        float scaleHeight = height / y;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(source, 0, 0, x, y, matrix, true);
    }
}
