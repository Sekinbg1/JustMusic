package ly.jj.newjustpiano.tools;

import Client.OnMessageListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.R;
import ly.jj.newjustpiano.views.PersonView;

import static java.lang.Thread.sleep;
import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.items.StaticItems.SONG;

public class StaticTools {
    public static void bindPeron(PersonView view, String name) {
        TextView nameView = view.findViewById(R.id.person_name);
        nameView.setText(name);
    }

    public static class OnClientMessage {
        protected void Error() {
        }

        ;

        protected void Message(byte[] data) {
        }

        ;
    }

    public static Context context;

    private final static Handler handler = new Handler(Looper.myLooper(), msg -> {
        switch (msg.what) {
            case 0:
                OnClientMessage onClientMessage = (OnClientMessage) msg.obj;
                onClientMessage.Error();
                return true;
            case 1:
                OnClientMessage onClientMessage1 = (OnClientMessage) ((Object[]) msg.obj)[0];
                byte[] data = (byte[]) ((Object[]) msg.obj)[1];
                onClientMessage1.Message(data);
                return true;
            case 3:
                Toast cant = Toast.makeText(context, "无法连接到服务器", Toast.LENGTH_SHORT);
                cant.show();
        }
        return false;
    });

    public static void sendMessageFuncAsync(byte MSGType, byte[] data, OnClientMessage message) {
        client.addOnMessageListener(MSGType, new OnMessageListener() {
            @Override
            public void onEnd() {
                Message msg = new Message();
                msg.what = 0;
                msg.obj = message;
                handler.sendMessage(msg);
            }

            @Override
            public void onMessage(byte[] bytes) {
                Message msg = new Message();
                msg.what = 1;
                msg.obj = new Object[]{message, bytes};
                handler.sendMessage(msg);
            }
        });
        sendMessage(MSGType, data);
    }

    public static byte[] sendMessageFuncSync(byte MSGType, byte[] data) {
        final byte[][] msg = new byte[1][];
        client.addOnMessageListener(MSGType, new OnMessageListener() {
            @Override
            public void onEnd() {
                msg[0] = new byte[0];
            }

            @Override
            public void onMessage(byte[] bytes) {
                msg[0] = bytes;
            }
        });
        sendMessage(MSGType, data);
        try {
            while (msg[0] == null) {
                sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msg[0];
    }

    public static void setOnlineHashKey(byte[] data) {
        onlineHashKey = data;
    }

    public static void sendMessageNoResponse(byte MSGType, byte[] data) {
        sendMessage(MSGType, data);
    }

    public static void sendMessage(byte MSGType, byte[] data) {
        if (client.isConnected()) {
            client.sendMessage(MSGType, data);
        } else {
            client.addOnMessageListener(RECONNECT, new OnMessageListener() {
                @Override
                public void onEnd() {
                    handler.sendEmptyMessage(3);
                }

                @Override
                public void onMessage(byte[] bytes) {

                }
            });
            client.sendMessage(RECONNECT, null);
            client.connect(Server, applicationProtocolId);
            client.sendMessage(MSGType, data);
        }
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
