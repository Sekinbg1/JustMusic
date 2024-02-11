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

import static ly.jj.newjustpiano.items.StaticItems.*;
import static ly.jj.newjustpiano.tools.StaticTools.sendMessageFuncAsync;


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
                        String salt = new String(data);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", user.getText().toString());
                        jsonObject.put("passwd", new String(Hash.hash((passwd.getText().toString() + salt).getBytes())));
                        sendMessageFuncAsync(LOGIN, jsonObject.toJSONString().getBytes(), new StaticTools.OnClientMessage() {
                            @Override
                            protected void Message(byte[] data) {
                                super.Message(data);
                                System.out.println(new String(data));
                                Intent intent = new Intent(context, OnlineMain.class);
                                intent.putExtra("message", new String(data));
                                startActivity(intent);
                            }
                        });
                    }
                });
            } else {
                msg.setText(getIntent().getStringExtra("message") + "暂不开放");
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
