package at.aau.anti_mon.client.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.multidex.MultiDex;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import javax.inject.Inject;

import at.aau.anti_mon.client.AntiMonopolyApplication;
import at.aau.anti_mon.client.R;
import at.aau.anti_mon.client.command.Commands;
import at.aau.anti_mon.client.events.GlobalEventQueue;
import at.aau.anti_mon.client.events.PinReceivedEvent;
import at.aau.anti_mon.client.json.JsonDataDTO;
import at.aau.anti_mon.client.json.JsonDataManager;
import at.aau.anti_mon.client.networking.WebSocketClient;

/**
 *
 */
public class StartNewGameActivity extends AppCompatActivity {

    EditText usernameEditText;

    String pin;

    @Inject
    WebSocketClient webSocketClient;

    /**
     * Dependency Injection of GlobalEventQueue
     */
    @Inject
    GlobalEventQueue globalEventQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_new_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.username);

        ((AntiMonopolyApplication) getApplication()).getAppComponent().inject(this);

    }


    /**
     * Event when the button "Create Game" is clicked
     * @param view View
     */
    public void onCreateGameClicked(View view) {
        String username = usernameEditText.getText().toString();

        if (username.isEmpty()) {
            Snackbar.make(view, "Please enter a username", BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }
        /*
         * Communication with Server -> send username to server -> get Pin
         */
        JsonDataDTO jsonData = new JsonDataDTO(Commands.CREATE_GAME, new HashMap<>());
        jsonData.putData("username", username);
        String jsonDataString = JsonDataManager.createJsonMessage(jsonData);
        webSocketClient.sendMessageToServer(jsonDataString);
        Log.println(Log.DEBUG, "Network", " Username sending for pin:" + jsonDataString);
    }

    /**
     * Event to receive a message
     * -> MAIN Thread to update UI
     * @param event ReceiveMessageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPinReceivedEvent(PinReceivedEvent event) {
        Log.d(this.getLocalClassName(), "Pin received: " + event.getPin());
        if (!isFinishing()) {
            pin = event.getPin();
            String username = usernameEditText.getText().toString();
            Intent intent = new Intent(this, LobbyActivity.class);
            intent.putExtra("pin", pin);
            intent.putExtra("username", username);
            startActivity(intent);
        } else {
            Log.d("LobbyActivity", "Activity is finishing. Cannot set pin.");
        }
    }



    public void onCancelStartNewGame(View view) {
        Intent intent = new Intent(StartNewGameActivity.this, StartMenuActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.d(this.getLocalClassName(), "EventBus registered");
        globalEventQueue.setEventBusReady(true);

        if (webSocketClient != null) {
            webSocketClient.connectToServer();
        }
    }

    @Override
    protected void onStop() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }

        EventBus.getDefault().unregister(this);
        Log.d("LobbyActivity", "EventBus unregistered");
        globalEventQueue.setEventBusReady(false);
        super.onStop();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
