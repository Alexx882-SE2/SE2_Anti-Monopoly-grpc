package at.aau.anti_mon.client;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Start_new_Game extends AppCompatActivity {
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        EditText username = findViewById(R.id.username);



        Button cancel = findViewById(R.id.newGame_cancel);
        cancel.setOnClickListener(
                v ->{
                    Intent intent = new Intent(Start_new_Game.this, Start_Page.class);
                    startActivity(intent);
                });

        Button create = findViewById(R.id.newGame_create);
        create.setOnClickListener(
                v ->{
                    String name = username.getText().toString();
                    if(!name.isEmpty()){
                        Lobby.username = name;
                        Intent intent = new Intent(Start_new_Game.this, Lobby.class);
                        startActivity(intent);
                    }
                });
    }
}
