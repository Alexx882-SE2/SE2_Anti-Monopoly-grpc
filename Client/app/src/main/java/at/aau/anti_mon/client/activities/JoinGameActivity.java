package at.aau.anti_mon.client.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.multidex.MultiDex;

import java.util.HashMap;

import javax.inject.Inject;

import at.aau.anti_mon.client.AntiMonopolyApplication;
import at.aau.anti_mon.client.R;
import at.aau.anti_mon.client.command.Commands;
import at.aau.anti_mon.client.json.JsonDataDTO;
import at.aau.anti_mon.client.json.JsonDataManager;
import at.aau.anti_mon.client.networking.WebSocketClient;

public class JoinGameActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText pinEditText;

    @Inject
    WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        usernameEditText = findViewById(R.id.editText_joinGame_Name);
        pinEditText = findViewById(R.id.editText_joinGame_Pin);

        /*
         * Injection des WebSocketClients
         */
        ((AntiMonopolyApplication) getApplication()).getAppComponent().inject(this);

    }

    public void onJoinGameClicked(View view) {
        String username = usernameEditText.getText().toString();
        String pin = pinEditText.getText().toString();

        // add Name to Websocket URI
        webSocketClient.setUserId(username);
        if (!webSocketClient.isConnected()){
            webSocketClient.connectToServer();
        }

        if (!username.isEmpty() && !pin.isEmpty()) {
            JsonDataDTO jsonData = new JsonDataDTO(Commands.JOIN_GAME, new HashMap<>());
            jsonData.putData("username", username);
            jsonData.putData("pin", pin);
            String jsonMessage = JsonDataManager.createJsonMessage(jsonData);
            webSocketClient.sendMessageToServer(jsonMessage);

            // Den Username nutzen:
            Intent intent = new Intent(this, LobbyActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("pin", pin);
            startActivity(intent);
            // TODO: Abfrage welche Spieler alle in der Lobby sind

        }else {
            Log.d("ANTI-MONOPOLY-DEBUG", "Username or pin is empty");
            // TODO: Popup anzeigen
        }
    }

    public void onCancelJoinGame(View view) {
        // Go back to last Activity on Stack (StartMenuActivity)
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Test for MessageQueue and Session holding
       //if (webSocketClient != null) {
            //webSocketClient.setUserId(usernameEditText.getText().toString());
       //     webSocketClient.connectToServer();
       //}
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Test for MessageQueue and Session holding
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}