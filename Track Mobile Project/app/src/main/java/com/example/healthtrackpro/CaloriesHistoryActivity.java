package com.example.healthtrackpro;

import android.graphics.Color; // Color sınıfını içeri aktarır, renkleri kullanmak için kullanılır.
import android.graphics.Typeface; // Typeface sınıfını içeri aktarır, yazı tipi stillerini kullanmak için kullanılır.
import android.os.Bundle; // Bundle sınıfını içeri aktarır, aktivite durumlarını kaydetmek için kullanılır.
import android.util.Log; // Log sınıfını içeri aktarır, uygulama günlüklerini yazmak için kullanılır.
import android.view.View; // View sınıfını içeri aktarır, kullanıcı arayüzü öğelerini yönetmek için kullanılır.
import android.widget.LinearLayout; // LinearLayout sınıfını içeri aktarır, lineer düzenleri kullanmak için kullanılır.
import android.widget.TextView; // TextView sınıfını içeri aktarır, metin görüntüleme alanlarını yönetmek için kullanılır.

import androidx.annotation.NonNull; // @NonNull anotasyonunu içeri aktarır, null olmayan parametreleri belirtmek için kullanılır.
import androidx.appcompat.app.AppCompatActivity; // AppCompatActivity sınıfını içeri aktarır, aktivite temel sınıfı olarak kullanılır.

import com.github.mikephil.charting.charts.BarChart; // BarChart sınıfını içeri aktarır, çubuk grafiklerini oluşturmak için kullanılır.
import com.github.mikephil.charting.components.Legend; // Legend sınıfını içeri aktarır, grafik göstergelerini yönetmek için kullanılır.
import com.github.mikephil.charting.components.XAxis; // XAxis sınıfını içeri aktarır, X eksenini yönetmek için kullanılır.
import com.github.mikephil.charting.components.YAxis; // YAxis sınıfını içeri aktarır, Y eksenini yönetmek için kullanılır.
import com.github.mikephil.charting.data.BarData; // BarData sınıfını içeri aktarır, çubuk grafik verilerini tutmak için kullanılır.
import com.github.mikephil.charting.data.BarDataSet; // BarDataSet sınıfını içeri aktarır, çubuk grafik veri setlerini yönetmek için kullanılır.
import com.github.mikephil.charting.data.BarEntry; // BarEntry sınıfını içeri aktarır, çubuk grafik girişlerini tutmak için kullanılır.
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter; // IndexAxisValueFormatter sınıfını içeri aktarır, X eksenindeki etiketleri biçimlendirmek için kullanılır.
import com.github.mikephil.charting.utils.ColorTemplate; // ColorTemplate sınıfını içeri aktarır, hazır renk paletlerini kullanmak için kullanılır.

import com.google.firebase.auth.FirebaseAuth; // FirebaseAuth sınıfını içeri aktarır, Firebase kimlik doğrulama hizmetini yönetmek için kullanılır.
import com.google.firebase.auth.FirebaseUser; // FirebaseUser sınıfını içeri aktarır, Firebase'e giriş yapmış kullanıcı bilgilerini tutmak için kullanılır.
import com.google.firebase.database.DataSnapshot; // DataSnapshot sınıfını içeri aktarır, veritabanından alınan anlık görüntüleri yönetmek için kullanılır.
import com.google.firebase.database.DatabaseError; // DatabaseError sınıfını içeri aktarır, veritabanı hatalarını yönetmek için kullanılır.
import com.google.firebase.database.DatabaseReference; // DatabaseReference sınıfını içeri aktarır, Firebase veritabanı referansını yönetmek için kullanılır.
import com.google.firebase.database.FirebaseDatabase; // FirebaseDatabase sınıfını içeri aktarır, Firebase veritabanını yönetmek için kullanılır.
import com.google.firebase.database.ValueEventListener; // ValueEventListener arayüzünü içeri aktarır, veritabanı değişikliklerini dinlemek için kullanılır.

import java.util.ArrayList; // ArrayList sınıfını içeri aktarır, dinamik listeler oluşturmak için kullanılır.
import java.util.List; // List arayüzünü içeri aktarır, liste yapılarını kullanmak için kullanılır.

public class CaloriesHistoryActivity extends AppCompatActivity {
    private BarChart caloriesChart; // Kalori geçmişini gösteren çubuk grafik
    private DatabaseReference caloriesRef; // Firebase veritabanında kalori geçmişi verilerine referans
    private FirebaseAuth mAuth; // Firebase kimlik doğrulama nesnesi
    private TextView noDataTextView; // Veri bulunamadığında gösterilen metin alanı
    private LinearLayout rootLayout; // Kök layout (gerekirse daha sonra kullanılabilir)
    private TextView totalCaloriesTextView; // Toplam kalori miktarını gösteren metin alanı
    private TextView averageCaloriesTextView; // Ortalama kalori miktarını gösteren metin alanı

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Aktiviteyi oluştururken üst sınıfın onCreate metodunu çağırır.
        setContentView(R.layout.activity_calories_history); // Layout dosyasını (activity_calories_history.xml) bu aktiviteye bağlar.

        caloriesChart = findViewById(R.id.caloriesChart); // Çubuk grafiği XML'deki ID'sine göre bulur.
        noDataTextView = findViewById(R.id.noDataTextView); // Veri yok metin alanını XML'deki ID'sine göre bulur.
        rootLayout = findViewById(R.id.rootLayout); // Kök layout XML'deki ID'sine göre bulur.
        totalCaloriesTextView = findViewById(R.id.totalCaloriesTextView); // Toplam kalori metin alanını XML'deki ID'sine göre bulur.
        averageCaloriesTextView = findViewById(R.id.averageCaloriesTextView); // Ortalama kalori metin alanını XML'deki ID'sine göre bulur.

        mAuth = FirebaseAuth.getInstance(); // Firebase kimlik doğrulama örneğini alır.
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Mevcut Firebase kullanıcısını alır.
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Kullanıcının benzersiz ID'sini alır.
            caloriesRef = FirebaseDatabase.getInstance().getReference("calories_history").child(userId); // Kullanıcının kalori verilerine referansı alır.
            loadCaloriesData(); // Kalori verilerini yükleme metodunu çağırır.
        }
    }

    // Kalori verilerini yükleme metodu
    private void loadCaloriesData() {
        caloriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>(); // Çubuk grafik girdilerini tutacak liste oluşturur.
                List<String> labels = new ArrayList<>(); // X ekseni etiketlerini tutacak liste oluşturur.
                int i = 0; // Girdi sayacını başlatır.
                double totalCalories = 0; // Toplam kaloriyi tutar.
                int validCaloriesCount = 0; // Geçerli kalori sayısını tutar.

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // Veritabanından alınan her bir anlık görüntü için döngüye girer.
                    String date = snapshot.getKey(); // Anlık görüntünün anahtarını (tarih) alır.
                    DataSnapshot caloriesSnapshot = snapshot.child("calories_amount"); // Anlık görüntüden kalori verisi alanını alır.
                    if (caloriesSnapshot.exists()){ // Eğer kalori verisi alanı varsa
                        Float calories = caloriesSnapshot.getValue(Float.class); // Kalori miktarını alır.
                        if (calories != null){ // Eğer kalori miktarı null değilse
                            entries.add(new BarEntry(i, calories)); // Grafik girdisine ekler.
                            labels.add(date); // X ekseni etiketine tarihi ekler.
                            totalCalories += calories; // Toplam kaloriye ekler.
                            validCaloriesCount++; // Geçerli kalori sayısını artırır.
                            i++; // Girdi sayacını artırır.
                        }else {
                            Log.e("CaloriesHistory","calories_amount değeri null :" + date); // Eğer kalori miktarı null ise hata mesajı yazdırır.
                        }
                    }else{
                        Log.e("CaloriesHistory","calories_amount key değeri bulunamadı :" + date); // Eğer kalori verisi alanı bulunamazsa hata mesajı yazdırır.
                    }
                }

                if (entries.isEmpty()) { // Eğer hiç veri girişi yoksa
                    noDataTextView.setVisibility(View.VISIBLE); // Veri yok metin alanını görünür yapar.
                    caloriesChart.setVisibility(View.GONE); // Grafiği gizler.
                    totalCaloriesTextView.setText("0 kcal"); // Toplam kalori metin alanını sıfır olarak ayarlar.
                    averageCaloriesTextView.setText("0 kcal"); // Ortalama kalori metin alanını sıfır olarak ayarlar.
                    Log.e("CaloriesHistory", "Firebase'de kalori verisi bulunamadı."); // Hata mesajı yazdırır.
                } else { // Eğer veri girişi varsa
                    noDataTextView.setVisibility(View.GONE); // Veri yok metin alanını gizler.
                    caloriesChart.setVisibility(View.VISIBLE); // Grafiği görünür yapar.

                    // Toplam ve ortalama kalori hesapla
                    double averageCalories = validCaloriesCount > 0 ? totalCalories / validCaloriesCount : 0; // Ortalama kaloriyi hesaplar.
                    totalCaloriesTextView.setText(String.format("%.2f kcal", totalCalories)); // Toplam kalori metin alanını günceller.
                    averageCaloriesTextView.setText(String.format("%.2f kcal", averageCalories)); // Ortalama kalori metin alanını günceller.
                    BarDataSet dataSet = new BarDataSet(entries, "Calories Burned (kcal)"); // Çubuk grafik veri setini oluşturur.
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Veri setine renk paleti uygular.
                    dataSet.setValueTextSize(12f); // Sütun üzerindeki değerlerin yazı boyutunu ayarlar.
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // Sütun üzerindeki değerlerin yazı kalınlığını ayarlar.
                    BarData barData = new BarData(dataSet); // Çubuk grafik verisini oluşturur.

                    // X Ekseni Ayarları
                    XAxis xAxis = caloriesChart.getXAxis(); // X eksenini alır.
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // X ekseni etiketlerini biçimlendirir.
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X eksenini altta konumlandırır.
                    xAxis.setGranularity(1f); // Her bir girdi için bir etiket gösterir.
                    xAxis.setLabelRotationAngle(-45f); // Etiketleri 45 derece eğik gösterir.
                    xAxis.setTextSize(12f); // X ekseni yazı boyutunu ayarlar.
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD); // X ekseni yazı kalınlığını ayarlar.


                    // Y Ekseni Ayarları
                    YAxis yAxisLeft = caloriesChart.getAxisLeft(); // Sol Y eksenini alır.
                    yAxisLeft.setAxisMinimum(0f); // Y ekseni minimum değerini ayarlar.
                    yAxisLeft.setGranularity(1f); // Y ekseni değerlerini tam sayı olarak gösterir.
                    yAxisLeft.setAxisLineColor(getResources().getColor(R.color.black)); // Y ekseni rengini siyah yapar.
                    yAxisLeft.setTextSize(12f); // Y ekseni yazı boyutunu ayarlar.
                    yAxisLeft.setTypeface(Typeface.DEFAULT_BOLD); // Y ekseni yazı kalınlığını ayarlar.

                    Legend legend = caloriesChart.getLegend(); // Grafik göstergesini alır.
                    legend.setTextSize(14f); // Gösterge yazı boyutunu ayarlar.
                    legend.setTypeface(Typeface.DEFAULT_BOLD); // Gösterge yazı kalınlığını ayarlar.

                    //Grafik Ayarları
                    caloriesChart.getAxisRight().setEnabled(false); // Sağ Y eksenini devre dışı bırakır.
                    caloriesChart.getDescription().setEnabled(false); // Grafik açıklamasını devre dışı bırakır.
                    caloriesChart.animateY(1000); // Y ekseninde animasyon uygular.
                    caloriesChart.setDrawGridBackground(true); // Grid arka planını aktif yapar.
                    caloriesChart.setGridBackgroundColor(getResources().getColor(R.color.light_grey)); // Grid arka plan rengini açık gri yapar.
                    caloriesChart.setDrawBorders(true); // Grafiğe sınır ekler.
                    caloriesChart.setBorderColor(getResources().getColor(R.color.black)); // Grafik sınır rengini siyah yapar.



                    caloriesChart.setData(barData); // Grafiğe veriyi yükler.
                    caloriesChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)); // X eksenini etiket formatını ayarlar.
                    caloriesChart.getDescription().setEnabled(false); // Grafik açıklamasını devre dışı bırakır.
                    caloriesChart.invalidate(); // Grafiği günceller.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CaloriesHistory", "Veri okuma iptal edildi: ", error.toException()); // Hata durumunda hata mesajı yazdırır.
            }
        });
    }
}