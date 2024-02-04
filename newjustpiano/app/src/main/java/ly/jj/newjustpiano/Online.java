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

import static ly.jj.newjustpiano.items.StaticItems.*;


public class Online extends Activity {
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
                JSONObject jsonObject = new JSONObject();
                client.addOnMessageListener(LOGIN, new OnMessageListener() {
                    @Override
                    public void onEnd() {

                    }

                    @Override
                    public void onMessage(byte[] bytes) {
                        System.out.println(new String(bytes));
                        Intent intent = new Intent(context, OnlineMain.class);
                        intent.putExtra("message", new String(bytes));
                        startActivity(intent);
                    }
                });
                client.addOnMessageListener(SALT, new OnMessageListener() {
                    @Override
                    public void onEnd() {

                    }

                    @Override
                    public void onMessage(byte[] bytes) {
                        String salt = new String(bytes);
                        System.out.println("geted salt:" + salt);
                            jsonObject.put("name", user.getText().toString());
                            jsonObject.put("passwd", new String(Hash.hash((passwd.getText().toString() + salt).getBytes())));
                            StaticItems.client.sendMessage(LOGIN, jsonObject.toString().getBytes());
                    }
                });
                System.out.println("get salt");
                StaticItems.client.sendMessage(SALT, null);
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
