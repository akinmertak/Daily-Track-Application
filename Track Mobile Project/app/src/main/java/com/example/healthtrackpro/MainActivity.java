package com.example.healthtrackpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the login activity (Giriş yapma aktivitesine yönlendirir)
                Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Giriş yapma aktivitesine gitmek için bir Intent oluşturur.
                startActivity(intent); // Giriş yapma aktivitesini başlatır.
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the signup activity (Kayıt olma aktivitesine yönlendirir)
                Intent intent = new Intent(MainActivity.this, SignupActivity.class); // Kayıt olma aktivitesine gitmek için bir Intent oluşturur.
                startActivity(intent); // Kayıt olma aktivitesini başlatır.
            }
        });
    }
}