package com.example.healthtrackpro;

public class User {
    private String username; // Kullanıcının adı
    private String email; // Kullanıcının e-posta adresi

    // Required default constructor for Firebase (Firebase için gerekli varsayılan yapıcı metot)
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        // DataSnapshot.getValue(User.class) çağrıları için varsayılan yapıcı metot gereklidir.
    }

    // Kullanıcı nesnesi oluşturmak için yapıcı metot
    public User(String username, String email) {
        this.username = username; // Kullanıcı adını ayarlar
        this.email = email; // Kullanıcı e-posta adresini ayarlar
    }

    // Kullanıcı adını almak için getter metot
    public String getUsername() {
        return username;
    }

    // Kullanıcı adını ayarlamak için setter metot
    public void setUsername(String username) {
        this.username = username;
    }

    // Kullanıcı e-posta adresini almak için getter metot
    public String getEmail() {
        return email;
    }

    // Kullanıcı e-posta adresini ayarlamak için setter metot
    public void setEmail(String email) {
        this.email = email;
    }
}