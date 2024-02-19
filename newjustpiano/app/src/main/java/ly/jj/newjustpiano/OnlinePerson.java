package ly.jj.newjustpiano;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.alibaba.fastjson2.JSONObject;
import ly.jj.newjustpiano.tools.StaticTools;

import static ly.jj.newjustpiano.items.StaticItems.PERSON_INFO;
import static ly.jj.newjustpiano.tools.StaticTools.sendMessageFuncAsync;

public class OnlinePerson extends ly.jj.newjustpiano.Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_person);
        TextView name = findViewById(R.id.online_person_name);
        TextView reg = findViewById(R.id.online_person_rt);
        TextView level = findViewById(R.id.online_person_level);
        TextView exp = findViewById(R.id.online_person_exp);
        TextView cl = findViewById(R.id.online_person_cl);
        /*
			Name    string `json:"name"`
			RegTime string `json:"regTime"`
			Level   int    `json:"level"`
			Exp     int    `json:"exp"`
			CLevel  int    `json:"cLevel"`
         */
        sendMessageFuncAsync(PERSON_INFO, null, new StaticTools.OnClientMessage() {
            @Override
            protected void Message(byte[] data) {
                super.Message(data);
                JSONObject jsonObject = JSONObject.parseObject(new String(data));
                name.setText(jsonObject.getString("name"));
                reg.setText(jsonObject.getString("regTime"));
                level.setText(jsonObject.getString("level"));
                exp.setText(jsonObject.getString("exp") + "/" + (jsonObject.getInteger("level") * 20 + 50));
                cl.setText(jsonObject.getString("cLevel"));
            }
        });
    }
}
