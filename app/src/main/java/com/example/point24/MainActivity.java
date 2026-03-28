package com.example.point24;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "Point24Prefs";
    private static final String KEY_DIFFICULTY = "difficulty";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    private static final String TAG = "Point24";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_main);
            
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            Button btnEasy = findViewById(R.id.btnEasy);
            Button btnMedium = findViewById(R.id.btnMedium);
            Button btnHard = findViewById(R.id.btnHard);
            Button btnExpert = findViewById(R.id.btnExpert);

            if (btnEasy != null) {
                btnEasy.setOnClickListener(v -> startGame(1));  // 入门
            }
            if (btnMedium != null) {
                btnMedium.setOnClickListener(v -> startGame(2));  // 容易
            }
            if (btnHard != null) {
                btnHard.setOnClickListener(v -> startGame(3));  // 中等
            }
            if (btnExpert != null) {
                btnExpert.setOnClickListener(v -> startGame(4));  // 困难
            }
            
            Log.d(TAG, "MainActivity created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startGame(int difficulty) {
        try {
            Log.d(TAG, "Starting game with difficulty: " + difficulty);
            
            // 保存难度选择
            prefs.edit().putInt(KEY_DIFFICULTY, difficulty).apply();

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(EXTRA_DIFFICULTY, difficulty);
            startActivity(intent);
            
            Log.d(TAG, "Intent started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting game: " + e.getMessage(), e);
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            int lastDifficulty = prefs.getInt(KEY_DIFFICULTY, 1);
            String message = "上次选择: ";
            switch (lastDifficulty) {
                case 1: message += "入门"; break;
                case 2: message += "容易"; break;
                case 3: message += "中等"; break;
                case 4: message += "困难"; break;
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
}