package ly.jj.newjustpiano;

import Client.OnMessageListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.Adapter.OnlineSongBankListAdapter;

import java.util.ArrayList;
import java.util.List;

import static ly.jj.newjustpiano.Local.setFlipperTouchListener;
import static ly.jj.newjustpiano.items.StaticItems.*;

public class OnlineBank extends Activity {
    Context context = this;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    JSONObject object = (JSONObject) msg.obj;
                    JSONArray array = object.getJSONArray("classesNames");
                    for (Object o : array) {
                        String bank = (String) o;
                        Button button = new Button(context);
                        button.setText(bank);
                        button.setTextSize(textSize);
                        button.setBackground(getDrawable(R.drawable.button_background_t));
                        button.setOnClickListener(v -> {
                            int i = flipper.getDisplayedChild();
                            int j = selectsList.indexOf(v);
                            if (i == j) return;
                            if (i > j) {
                                flipper.setInAnimation(context, R.anim.slide_left_in);
                                flipper.setOutAnimation(context, R.anim.slide_right_out);
                            } else {
                                flipper.setInAnimation(context, R.anim.slide_right_in);
                                flipper.setOutAnimation(context, R.anim.slide_left_out);
                            }
                            flipper.setDisplayedChild(j);
                            for (Button b : selectsList) {
                                b.setTextSize(textSize);
                            }
                            ((Button) v).setTextSize((float) (textSize * 1.5));
                            System.out.println("123");
                        });
                        selectsList.add(button);
                        flipper_select.addView(button);
                        GridView gridView = new GridView(context);
                        gridView.setNumColumns(3);
                        gridView.setAdapter(new OnlineSongBankListAdapter(context, object.getJSONObject("classes").getJSONObject(bank), bank, 3));
                        setFlipperTouchListener(gridView, flipper, selectsList);
                        flipper.addView(gridView);
                    }
                    if (!selectsList.isEmpty())
                        selectsList.get(0).setTextSize((float) (textSize * 1.5));
            }
        }
    };

    private final List<Button> selectsList = new ArrayList<>();

    private float textSize;
    private LinearLayout flipper_select;
    private ViewFlipper flipper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_banks);
        textSize = new TextView(this).getTextSize() / getResources().getDisplayMetrics().density;
        flipper_select = findViewById(R.id.online_bank_flipper_select);
        flipper = findViewById(R.id.online_bank_flipper);
        client.addOnMessageListener(CLASS, new OnMessageListener() {
            @Override
            public void onEnd() {

            }

            @Override
            public void onMessage(byte[] bytes) {
                JSONObject object = JSONObject.parseObject(new String(bytes));
                Message msg = new Message();
                msg.what = 0;
                msg.obj = object;
                handler.sendMessage(msg);
            }
        });
        client.sendMessage(CLASS, null);
    }
}
