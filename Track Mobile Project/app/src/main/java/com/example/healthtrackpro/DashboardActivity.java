package com.example.healthtrackpro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView username, sleepTextView, waterTextView;
    private ProgressBar sleepProgressBar, waterProgressBar;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    private TextView stepsTextView, distanceTextView, energyTextView;
    private ProgressBar stepProgressBar, distanceProgressBar, energyProgressBar;
    private int stepsCount = 0;
    private int waterIntake = 0;
    private static final int WATER_GOAL = 2500;
    private float distanceCovered = 0;
    private float caloriesBurned = 0;
    private float sleepHours = 0;
    private static final int STEP_GOAL = 10000;
    private static final float DISTANCE_GOAL = 7.5f;
    private static final float CALORIES_GOAL = 2000;
    private static final int SLEEP_GOAL = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Firebase Initialization (Firebase'i başlatır)
        mAuth = FirebaseAuth.getInstance(); // Firebase kimlik doğrulama örneğini alır.
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Mevcut Firebase kullanıcısını alır.
        usersRef = FirebaseDatabase.getInstance().getReference("users"); // "users" düğümüne referansı alır.

        // Layout Elements Initialization (Arayüz öğelerini başlatır)
        username = findViewById(R.id.username);
        stepsTextView = findViewById(R.id.stepsTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        energyTextView = findViewById(R.id.energyTextView);
        sleepTextView = findViewById(R.id.sleepTextView);
        waterTextView = findViewById(R.id.waterTextView);

        stepProgressBar = findViewById(R.id.stepProgressBar);
        distanceProgressBar = findViewById(R.id.distanceProgressBar);
        energyProgressBar = findViewById(R.id.energyProgressBar);
        sleepProgressBar = findViewById(R.id.sleepProgressBar);
        waterProgressBar = findViewById(R.id.waterProgressBar);

        Button addWaterButton = findViewById(R.id.addWaterButton);
        Button addStepsButton = findViewById(R.id.addStepsButton);
        Button addDistanceButton = findViewById(R.id.addDistanceButton);
        Button addCaloriesButton = findViewById(R.id.addCaloriesButton);
        Button addSleepButton = findViewById(R.id.addSleepButton);
        ImageView logoutImageView = findViewById(R.id.imageView3);
        sharedPreferences = getSharedPreferences("HealthTrackPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();


        // Retry Activities Button (Aktiviteleri Sıfırla Butonu)
        Button retryActivitiesButton = findViewById(R.id.retryActivitiesButton);
        loadUserData();

        logoutImageView.setOnClickListener(v -> { // Çıkış resmine tıklama olayı dinleyicisi
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class); // Ana sayfaya gitmek için Intent oluşturur.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Aktivite geçmişini temizlemek için bayraklar ekler.
            startActivity(intent); // Ana sayfayı başlatır.
            finish(); // Mevcut aktiviteyi kapatır.
        });

        // Fetch user information from Firebase (Firebase'den kullanıcı bilgilerini getir)
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Kullanıcının benzersiz ID'sini alır.
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() { // Tek seferlik kullanıcı bilgisi almak için ValueEventListener ekler.
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) { // Eğer veri varsa
                        String userName = dataSnapshot.child("username").getValue(String.class); // Kullanıcı adını alır.
                        username.setText(userName); // Kullanıcı adı metin alanına yazdırır.
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors (Hataları yönetir)
                }
            });
        }


        // Add click listeners for activity buttons (Aktivite butonları için tıklama dinleyicilerini ekler)
        addStepsButton.setOnClickListener(v -> {
            stepsCount += 500;
            updateStepsUI();

        });

        addDistanceButton.setOnClickListener(v -> {
            distanceCovered += 0.5f;
            updateDistanceUI();

        });

        addCaloriesButton.setOnClickListener(v -> {
            caloriesBurned += 100;
            updateCaloriesUI();

        });

        addSleepButton.setOnClickListener(v -> {
            sleepHours += 1;
            updateSleepUI();

        });

        addWaterButton.setOnClickListener(v -> {
            waterIntake += 250;
            updateWaterUI();

        });

        retryActivitiesButton.setOnClickListener(v -> {
            // Reset all activities (Tüm aktiviteleri sıfırla)
            stepsCount = 0;
            waterIntake = 0;
            distanceCovered = 0;
            caloriesBurned = 0;
            sleepHours = 0;

            // Update UI with reset values (Arayüzü sıfırlanmış değerlerle güncelle)
            updateStepsUI();
            updateDistanceUI();
            updateCaloriesUI();
            updateSleepUI();
            updateWaterUI();

            // Reset progress bars (İlerleme çubuklarını sıfırla)
            stepProgressBar.setProgress(0);
            distanceProgressBar.setProgress(0);
            energyProgressBar.setProgress(0);
            sleepProgressBar.setProgress(0);
            waterProgressBar.setProgress(0);

        });
        sleepTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SleepHistoryActivity.class);
            startActivity(intent);
        });
        stepsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StepsHistoryActivity.class);
            startActivity(intent);
        });

        energyTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CaloriesHistoryActivity.class);
            startActivity(intent);
        });


        distanceTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DistanceHistoryActivity.class);
            startActivity(intent);
        });

        waterTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, WaterHistoryActivity.class);
            startActivity(intent);
        });
    }

    // Kullanıcı verilerini kaydetme metodu
    private void saveUserData() {
        editor.putInt("stepsCount", stepsCount);
        editor.putInt("waterIntake", waterIntake);
        editor.putFloat("distanceCovered", distanceCovered);
        editor.putFloat("caloriesBurned", caloriesBurned);
        editor.putFloat("sleepHours", sleepHours);
        editor.apply(); // Verileri kaydeder.
    }
    // Kullanıcı verilerini yükleme metodu
    private void loadUserData() {
        stepsCount = sharedPreferences.getInt("stepsCount", 0);
        waterIntake = sharedPreferences.getInt("waterIntake", 0);
        distanceCovered = sharedPreferences.getFloat("distanceCovered", 0);
        caloriesBurned = sharedPreferences.getFloat("caloriesBurned", 0);
        sleepHours = sharedPreferences.getFloat("sleepHours", 0);

        updateStepsUI();
        updateWaterUI();
        updateDistanceUI();
        updateCaloriesUI();
        updateSleepUI();
    }

    // Su verilerini Firebase'e kaydetme metodu
    private void saveWaterDataToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Mevcut Firebase kullanıcısını alır.
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Kullanıcının benzersiz ID'sini alır.
            DatabaseReference waterRef = FirebaseDatabase.getInstance().getReference("water_history").child(userId);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            HashMap<String, Object> waterData = new HashMap<>();
            waterData.put("water_amount", waterIntake); // Su miktarını HashMap'e ekler.
            waterRef.child(currentDate).setValue(waterData); // Veritabanına tarihi ve su verilerini kaydeder.
        }
    }

    private void saveSleepDataToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference sleepRef = FirebaseDatabase.getInstance().getReference("sleep_history").child(userId);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            HashMap<String, Object> sleepData = new HashMap<>();
            sleepData.put("sleep_amount", sleepHours);

            sleepRef.child(currentDate).setValue(sleepData);
        }
    }


    // Adım verilerini Firebase'e kaydetme metodu
    private void saveStepsDataToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference stepsRef = FirebaseDatabase.getInstance().getReference("steps_history").child(userId);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            HashMap<String,Object> stepsData = new HashMap<>();
            stepsData.put("step_amount",stepsCount);

            stepsRef.child(currentDate).setValue(stepsData);
        }
    }

    // Mesafe verilerini Firebase'e kaydetme metodu
    private void saveDistanceDataToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference distanceRef = FirebaseDatabase.getInstance().getReference("distance_history").child(userId);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            HashMap<String, Object> distanceData = new HashMap<>();
            distanceData.put("distance_amount", distanceCovered);


            distanceRef.child(currentDate).setValue(distanceData);
        }
    }
    // Kalori verilerini Firebase'e kaydetme metodu
    private void saveCaloriesDataToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference caloriesRef = FirebaseDatabase.getInstance().getReference("calories_history").child(userId);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            HashMap<String,Object> caloriesData = new HashMap<>();
            caloriesData.put("calories_amount",caloriesBurned);

            caloriesRef.child(currentDate).setValue(caloriesData);

        }
    }


    // Su arayüzünü güncelleme metodu
    private void updateWaterUI() {
        // Update water intake information and progress (Su tüketimi bilgilerini ve ilerlemeyi günceller)
        waterTextView.setText(waterIntake + " / " + WATER_GOAL + " ml"); // Su miktarını metin alanına yazar.
        int progress = (int) ((waterIntake / (float) WATER_GOAL) * 100); // Su tüketimi ilerlemesini hesaplar.
        waterProgressBar.setProgress(progress); // Su ilerleme çubuğunu günceller.

        if (waterIntake == WATER_GOAL) { // Eğer su hedefine ulaşıldıysa
            Toast.makeText(this, "Water goal achieved! Great job!", Toast.LENGTH_LONG).show(); // Başarı mesajı gösterir.

        }

        saveUserData(); // Kullanıcı verilerini kaydeder.
        saveWaterDataToFirebase(); // Su verilerini Firebase'e kaydeder.
    }

    private void updateStepsUI() {
        stepsTextView.setText(stepsCount + " / " + STEP_GOAL + " steps");
        int progress = (int) ((stepsCount / (float) STEP_GOAL) * 100);
        stepProgressBar.setProgress(progress);

        if (stepsCount == STEP_GOAL) {
            Toast.makeText(this, "Step goal achieved! Great job!", Toast.LENGTH_LONG).show();
        }
        saveUserData();
        saveStepsDataToFirebase();
    }

    // Mesafe arayüzünü güncelleme metodu
    private void updateDistanceUI() {
        distanceTextView.setText(String.format("%.2f / %.2f km", distanceCovered, DISTANCE_GOAL));
        int progress = (int) ((distanceCovered / DISTANCE_GOAL) * 100);
        distanceProgressBar.setProgress(progress);

        if (distanceCovered == DISTANCE_GOAL) {
            Toast.makeText(this, "Distance goal achieved! Great job!", Toast.LENGTH_LONG).show();
        }
        saveUserData();
        saveDistanceDataToFirebase();
    }

    // Kalori arayüzünü güncelleme metodu
    private void updateCaloriesUI() {
        energyTextView.setText(caloriesBurned + " / " + CALORIES_GOAL + " kcal");
        int progress = (int) ((caloriesBurned / (float) CALORIES_GOAL) * 100);
        energyProgressBar.setProgress(progress);

        if (caloriesBurned == CALORIES_GOAL) {
            Toast.makeText(this, "Calories goal achieved! Great job!", Toast.LENGTH_LONG).show();
        }
        saveUserData();
        saveCaloriesDataToFirebase();
    }

    // Uyku arayüzünü güncelleme metodu
    private void updateSleepUI() {
        sleepTextView.setText(sleepHours + " / " + SLEEP_GOAL + " hours");
        int progress = (int) ((sleepHours /  SLEEP_GOAL) * 100);
        sleepProgressBar.setProgress(progress);

        if (sleepHours == SLEEP_GOAL) {
            Toast.makeText(this, "Sleep goal achieved! Great job!", Toast.LENGTH_LONG).show();
        }
        saveUserData();
        saveSleepDataToFirebase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveUserData();
    }
}