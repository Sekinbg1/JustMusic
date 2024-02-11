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
    public  static class OnClientMessage {
        protected  void Error(){};

        protected   void Message(byte[] data){};
    }

    public static void sendMessageFuncAsync(byte MSGType, byte[] data, OnClientMessage message) {
        client.addOnMessageListener(MSGType, new OnMessageListener() {
            @Override
            public void onEnd() {
                message.Error();
            }

            @Override
            public void onMessage(byte[] bytes) {
                message.Message(bytes);
            }
        });
        client.sendMessage(MSGType, data);
    }

    public static byte[] sendMessageFuncSync(byte MSGType, byte[] data) {
        final byte[][] msg = {new byte[0]};
        client.addOnMessageListener(MSGType, new OnMessageListener() {
            @Override
            public void onEnd() {
                msg[0] = new byte[1];
            }

            @Override
            public void onMessage(byte[] bytes) {
                msg[0] = bytes;
            }
        });
        client.sendMessage(MSGType, data);
        try {
            while (msg[0].length == 0) {
                sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msg[0];
    }

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
        object1.put("class", Class);
        object1.put("bank", bank);
        object1.put("song", Song);
        return JSONObject.parseObject(new String(sendMessageFuncSync(SONG, object1.toJSONString().getBytes())));
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
