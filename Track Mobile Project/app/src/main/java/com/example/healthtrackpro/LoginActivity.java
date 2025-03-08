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

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button loginButton;
    private TextView signUpLink;

    private FirebaseAuth mAuth; // Firebase kimlik doğrulama nesnesi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth (Firebase kimlik doğrulamasını başlatır)
        mAuth = FirebaseAuth.getInstance(); // Firebase kimlik doğrulama örneğini alır.

        // Initialize views (Arayüz öğelerini başlatır)
        editTextUsername = findViewById(R.id.usernameInput);
        editTextPassword = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpLink = findViewById(R.id.signUpLink);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserWithEmailAndPassword(); // Giriş yap butonuna tıklandığında kullanıcıyı e-posta ve şifre ile giriş yapma metodunu çağırır.
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)); // Kayıt ol linkine tıklandığında kayıt olma sayfasına geçiş yapar.
            }
        });
    }

    // Kullanıcıyı e-posta ve şifre ile giriş yapma metodu
    private void loginUserWithEmailAndPassword() {
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password) // Firebase kimlik doğrulamasına e-posta ve şifre ile giriş yapma isteği gönderir.
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information (Giriş başarılıysa, kullanıcı bilgilerini günceller)
                            FirebaseUser user = mAuth.getCurrentUser(); // Giriş yapmış kullanıcının bilgilerini alır.
                            Toast.makeText(LoginActivity.this, "Authentication successful.", // Başarılı bir şekilde giriş yapıldığında bilgilendirme mesajı gösterir.
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class)); // Giriş başarılıysa ana panele geçiş yapar.
                        } else {
                            // If sign in fails, display a message to the user. (Giriş başarısızsa, kullanıcıya mesaj gösterir.)
                            Toast.makeText(LoginActivity.this, "Authentication failed.", // Giriş başarısız olduğunda bilgilendirme mesajı gösterir.
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}