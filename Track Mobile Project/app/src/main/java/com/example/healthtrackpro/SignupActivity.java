package com.example.healthtrackpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmailAddress;
    private EditText editTextPassword;
    private Button getStartedButton;
    private TextView logInLink;

    private FirebaseAuth mAuth; // Firebase kimlik doğrulama nesnesi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth (Firebase kimlik doğrulamasını başlatır)
        mAuth = FirebaseAuth.getInstance(); // Firebase kimlik doğrulama örneğini alır.

        // Initialize views (Arayüz öğelerini başlatır)
        editTextUsername = findViewById(R.id.editTextText);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        getStartedButton = findViewById(R.id.button);
        logInLink = findViewById(R.id.logInLink);

        // Button click listeners (Buton tıklama olay dinleyicilerini ayarlar)
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser(); // Kayıt ol butonuna tıklandığında kullanıcıyı kaydetme metodunu çağırır.
            }
        });

        logInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)); // Giriş yap linkine tıklandığında giriş yapma sayfasına geçiş yapar.
            }
        });
    }

    // Kullanıcıyı kaydetme metodu
    private void signUpUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmailAddress.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password) // Firebase kimlik doğrulama ile e-posta ve şifre kullanarak yeni bir kullanıcı oluşturur.
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser(); // Oluşturulan kullanıcının bilgilerini alır.
                            if (user != null) {
                                String userId = user.getUid(); // Kullanıcının benzersiz ID'sini alır.
                                // Yeni kullanıcı eklendiğinde ilerleme verileri sıfırlanır
                                saveInitialData(userId, username); // Kullanıcının ilerleme verilerini sıfırlar ve kaydeder.
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class)); // Kayıt başarılıysa giriş yapma sayfasına geçiş yapar.
                                finish(); // Bu aktiviteyi kapatır.
                            }
                            Toast.makeText(SignupActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show(); // Kayıt başarılı olduğunda bilgilendirme mesajı gösterir.
                            // Redirect to LoginActivity after successful signup (Başarılı kayıt sonrası giriş yapma sayfasına yönlendirir)

                        } else {
                            Toast.makeText(SignupActivity.this, "Sign up failed. Please try again.", // Kayıt başarısız olduğunda bilgilendirme mesajı gösterir.
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Kullanıcının başlangıç verilerini kaydetme metodu
    private void saveInitialData(String userId, String userName) {

        // Kullanıcı bilgilerini kaydet
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        HashMap<String,String> userData = new HashMap<>();
        userData.put("username",userName);
        usersRef.child(userId).setValue(userData);


        // İlerleme verilerini kaydet
        DatabaseReference waterRef = FirebaseDatabase.getInstance().getReference("water_history").child(userId);
        DatabaseReference sleepRef = FirebaseDatabase.getInstance().getReference("sleep_history").child(userId);
        DatabaseReference stepsRef = FirebaseDatabase.getInstance().getReference("steps_history").child(userId);
        DatabaseReference distanceRef = FirebaseDatabase.getInstance().getReference("distance_history").child(userId);
        DatabaseReference caloriesRef = FirebaseDatabase.getInstance().getReference("calories_history").child(userId);


        // Başlangıç değerlerini 0 olarak ayarla veya boş bırak (Başlangıç değerlerini sıfırlar veya boş bırakır)
        waterRef.setValue(null);
        sleepRef.setValue(null);
        stepsRef.setValue(null);
        distanceRef.setValue(null);
        caloriesRef.setValue(null);

    }
}