package at.aau.anti_mon.client.activities;

import static at.aau.anti_mon.client.AntiMonopolyApplication.DEBUG_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import javax.inject.Inject;

import at.aau.anti_mon.client.AntiMonopolyApplication;
import at.aau.anti_mon.client.adapters.UserAdapter;
import at.aau.anti_mon.client.R;
import at.aau.anti_mon.client.enums.Commands;
import at.aau.anti_mon.client.events.DiceNumberReceivedEvent;
import at.aau.anti_mon.client.events.GlobalEventQueue;
import at.aau.anti_mon.client.events.HeartBeatEvent;
import at.aau.anti_mon.client.game.User;
import at.aau.anti_mon.client.json.JsonDataDTO;
import at.aau.anti_mon.client.json.JsonDataManager;
import at.aau.anti_mon.client.networking.WebSocketClient;

public class ActivityGameField extends AppCompatActivity {
    private static final int MAX_FIELD_COUNT = 40;
    private Random random;
    ArrayList<User> users;
    UserAdapter userAdapter;
    RecyclerView recyclerView;
    User currentUser;
    String pin;

    @Inject
    WebSocketClient webSocketClient;
    @Inject
    GlobalEventQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gamefield);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUI();
        processIntent();
        random = new Random();

        ((AntiMonopolyApplication) getApplication()).getAppComponent().inject(this);
    }

    private void initUI() {
        recyclerView = findViewById(R.id.players_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void processIntent() {
        Intent intent = getIntent();
        if (!intent.hasExtra("users") || !intent.hasExtra("currentUser") || !intent.hasExtra("pin")) {
            Log.e(DEBUG_TAG, "Intent is missing extras");
            finish();
            return;
        }
        users = new ArrayList<>();
        User[] usersList = JsonDataManager.parseJsonMessage(intent.getStringExtra("users"), User[].class);
        Collections.addAll(users, usersList);
        users.forEach(user -> Log.d(DEBUG_TAG, "User: " + user.getUsername() + " isOwner: " + user.isOwner() + " isReady: " + user.isReady() + " money: " + user.getMoney()));
        currentUser = JsonDataManager.parseJsonMessage(intent.getStringExtra("currentUser"), User.class);
        if (currentUser != null) {
            Log.d(DEBUG_TAG, "Current User: " + currentUser.getUsername() + " isOwner: " + currentUser.isOwner() + " isReady: " + currentUser.isReady() + " money: " + currentUser.getMoney());
        }
        currentUser = users.stream().filter(user -> user.getUsername().equals(currentUser.getUsername())).findFirst().orElse(null);
        userAdapter = new UserAdapter(users, currentUser);
        recyclerView.setAdapter(userAdapter);
        pin = intent.getStringExtra("pin");
        // show the current role of the user in a popup
        Intent i = new Intent(getApplicationContext(), PopActivityRole.class);
        i.putExtra("role", currentUser.getRole().name());
        i.putExtra("username", currentUser.getUsername());
        i.putExtra("figure", currentUser.getFigure().name());
        startActivity(i);
    }

    public void onSettings(View view) {
        Intent i = new Intent(getApplicationContext(), PopActivitySettings.class);
        startActivity(i);
    }

    public void onHandel(View view) {
        Intent i = new Intent(getApplicationContext(), PopActivityHandel.class);
        startActivity(i);
    }

    public void onObjects(View view) {
        Intent i = new Intent(getApplicationContext(), PopActivityObjects.class);
        startActivity(i);
    }

    public void onEndGame(View view) {
        // only for now, because the server does not support END_GAME yet
        JsonDataDTO jsonDataDTO = new JsonDataDTO(Commands.LEAVE_GAME, new HashMap<>());
        jsonDataDTO.putData("username", currentUser.getUsername());
        jsonDataDTO.putData("pin", pin);
        webSocketClient.sendJsonData(jsonDataDTO);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        queue.setEventBusReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onFigureMove(View view) {
        int randomNumber = random.nextInt(11) + 2;
        String dice = String.valueOf(randomNumber);
        String user = currentUser.getUsername();
        JsonDataDTO jsonData = new JsonDataDTO(Commands.DICENUMBER, new HashMap<>());
        jsonData.putData("dicenumber", dice);
        jsonData.putData("username", user);
        String jsonDataString = JsonDataManager.createJsonMessage(jsonData);
        webSocketClient.sendMessageToServer(jsonDataString);
        Log.println(Log.DEBUG, "ActivityGameField", "Send dicenumber to server.");

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHeartBeatEvent(HeartBeatEvent event) {

        Log.d("ANTI-MONOPOLY-DEBUG", "HeartBeatEvent");


        JsonDataDTO jsonData = new JsonDataDTO(Commands.HEARTBEAT, new HashMap<>());
        jsonData.putData("msg", "PONG");
        String jsonMessage = JsonDataManager.createJsonMessage(jsonData);
        webSocketClient.sendMessageToServer(jsonMessage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiceNumberReceivedEvent(DiceNumberReceivedEvent event) {
        int diceNumber = event.getDicenumber();
        String name = event.getFigure();
        int location = event.getLocation();
        if (name == null) {
            Log.d("onDiceNumberReceivedEvent", "name is null");
            return;
        }
        if (diceNumber < 1 || diceNumber > 12) {
            Log.d("onDiceNumberReceivedEvent", "diceNumber is out of range, should be between 2 and 12");
            return;
        }

        ImageView figure = findViewById(getID(name, null));
        moveFigure(location, diceNumber, figure);
    }

    private void moveFigure(int location, int diceNumber, ImageView figure) {
        for (int i = 1; i <= diceNumber; i++) {
            if (location == MAX_FIELD_COUNT) {
                location = 0;
            }
            location++;
            Log.d("moveFigure", "location: " + location);
            ImageView field = findViewById(getID(String.valueOf(location), "field"));
            figure.setX(field.getX());
            figure.setY(field.getY());
        }
    }

    public int getID(String fieldId, String prefix) {
        String resourceName = (prefix != null) ? prefix + fieldId : fieldId;
        return getResources().getIdentifier(resourceName, "id", getPackageName());
    }
}