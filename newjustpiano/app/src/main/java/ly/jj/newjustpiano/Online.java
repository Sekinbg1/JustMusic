package ly.jj.newjustpiano;

import Client.OnMessageListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.items.StaticItems;
import ly.jj.newjustpiano.tools.Hash;
import ly.jj.newjustpiano.tools.StaticTools;

import java.util.Arrays;

import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.tools.StaticTools.*;


public class Online extends ly.jj.newjustpiano.Activity {
    Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online);
        EditText user = findViewById(R.id.online_account);
        EditText passwd = findViewById(R.id.online_passwd);
        user.setText("12345");
        passwd.setText("12345");

        TextView msg = findViewById(R.id.online_message);
        msg.setText(getIntent().getStringExtra("message"));
        findViewById(R.id.online_login).setOnClickListener(view -> {
            if (getIntent().getBooleanExtra("isOpen", false)) {
                if (user.getText().toString().length() < 5) {
                    msg.setText("请输入正确的用户名");
                    return;
                }
                if (passwd.getText().toString().length() < 5) {
                    msg.setText("请输入正确的密码");
                    return;
                }
                sendMessageFuncAsync(SALT, null, new StaticTools.OnClientMessage() {
                    @Override
                    protected void Message(byte[] data) {
                        super.Message(data);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", user.getText().toString());
                        byte[] hash1 = Hash.hash(passwd.getText().toString().getBytes());
                        byte[] hash2 = new byte[hash1.length + data.length];
                        System.arraycopy(hash1, 0, hash2, 0, hash1.length);
                        System.arraycopy(data, 0, hash2, hash1.length, data.length);
                        byte[] hash = Hash.hash(hash2);
                        int[] uhash = new int[hash.length];
                        for (int i = 0; i < hash.length; i++) {
                            uhash[i] = hash[i] & 0xff;
                        }
                        jsonObject.put("passwd", uhash);
                        sendMessageFuncAsync(LOGIN, jsonObject.toJSONString().getBytes(), new StaticTools.OnClientMessage() {
                            @Override
                            protected void Message(byte[] data) {
                                super.Message(data);
                                byte login = data[0];
                                data = Arrays.copyOfRange(data, 1, data.length);
                                if (login == 0) {
                                    JSONObject object = JSONObject.parseObject(new String(data));
                                    setOnlineHashKey(jsonObject.getBytes("key"));
                                    Intent intent = new Intent(context, OnlineMain.class);
                                    intent.putExtra("message", object.getString("msg"));
                                    startActivity(intent);
                                } else {
                                    msg.setText(new String(data));
                                }
                            }
                        });
                    }
                });
            } else {
                msg.setText(getIntent().getStringExtra("message"));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.close();
        client = null;
    }
}
