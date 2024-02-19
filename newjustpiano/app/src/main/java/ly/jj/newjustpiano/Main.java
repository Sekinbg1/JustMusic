package ly.jj.newjustpiano;

import Client.OnMessageListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;
import ly.jj.newjustpiano.items.StaticItems;
import ly.jj.newjustpiano.tools.StaticTools;

import java.util.Arrays;

import static java.lang.Thread.sleep;
import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.tools.StaticTools.sendMessage;
import static ly.jj.newjustpiano.tools.StaticTools.sendMessageFuncAsync;

public class Main extends ly.jj.newjustpiano.Activity {

    @SuppressLint("StaticFieldLeak")
    Context context = this;
    public Handler handler = new Handler(Looper.myLooper(), msg -> {
        client.connect(Server2, applicationProtocolId);
        sendMessage(MSG, null);
        Toast toast = Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT);
        toast.show();
        return true;
    });
    private boolean exit;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);
        findViewById(R.id.main_local).setOnClickListener(v -> {
            Intent intent = new Intent(this, Local.class);
            startActivity(intent);
        });
        findViewById(R.id.main_online).setOnClickListener(v -> {
            client = new Client.ConnectClient();
            client.init();
            client.addOnMessageListener(MSG, new OnMessageListener() {
                @Override
                public void onEnd() {
                    if (StaticItems.client != null && !StaticItems.client.isConnected()) {
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = "无法连接到服务器";
                        handler.sendMessage(msg);
                    }
                }

                @Override
                public void onMessage(byte[] data) {
                    int state = data[0];
                    String msg = new String(Arrays.copyOfRange(data, 1, data.length));
                    if (state == 2) {
                        Message emsg = new Message();
                        emsg.what = 0;
                        emsg.obj = msg;
                        handler.sendMessage(emsg);
                    } else {
                        Intent intent = new Intent(context, Online.class);
                        intent.putExtra("message", msg);
                        intent.putExtra("isOpen", state == 0);
                        startActivity(intent);
                    }
                }
            });
            client.connect(Server, applicationProtocolId);
            sendMessage(MSG, null);
        });
        findViewById(R.id.main_songs).setOnClickListener(v -> {
            Intent intent = new Intent(this, Songs.class);
            startActivity(intent);
        });
        findViewById(R.id.main_setting).setOnClickListener(v -> {
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (exit) {
                System.exit(0);
            } else {
                exit = true;
                new Thread(() -> {
                    try {
                        sleep(3000);
                    } catch (InterruptedException ignore) {
                    }
                    exit = false;
                }).start();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
