package ly.jj.newjustpiano;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;

import static ly.jj.newjustpiano.items.StaticItems.LOGOUT;
import static ly.jj.newjustpiano.tools.StaticTools.sendMessageNoResponse;

public class OnlineMain extends ly.jj.newjustpiano.Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_main);
        View bank = findViewById(R.id.online_main_bank);
        View hall = findViewById(R.id.online_main_hall);
        View friend = findViewById(R.id.online_main_friend);
        View person = findViewById(R.id.online_main_person);

        bank.setOnClickListener(view -> {
            Intent intent = new Intent(this, OnlineBank.class);
            startActivity(intent);
        });
        hall.setOnClickListener(view -> {
            Intent intent = new Intent(this, OnlinePlay.class);
            startActivity(intent);
        });
        friend.setOnClickListener(view -> {
            Intent intent = new Intent(this, OnlineFriends.class);
            startActivity(intent);
        });
        person.setOnClickListener(view -> {
            Intent intent = new Intent(this, OnlinePerson.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendMessageNoResponse(LOGOUT, null);
    }
}
