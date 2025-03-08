package com.example.healthtrackpro;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DistanceHistoryActivity extends AppCompatActivity {
    private BarChart distanceChart;
    private DatabaseReference distanceRef;
    private FirebaseAuth mAuth;
    private TextView noDataTextView;
    private LinearLayout rootLayout;
    private TextView totalDistanceTextView;
    private TextView averageDistanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_history);

        distanceChart = findViewById(R.id.distanceChart);
        noDataTextView = findViewById(R.id.noDataTextView);
        rootLayout = findViewById(R.id.rootLayout);
        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        averageDistanceTextView = findViewById(R.id.averageDistanceTextView);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            distanceRef = FirebaseDatabase.getInstance().getReference("distance_history").child(userId);
            loadDistanceData();
        }
    }

    // Mesafe verilerini yükleme metodu
    private void loadDistanceData() {
        distanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int i = 0;
                double totalDistance = 0;
                int validDistanceCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.getKey(); // Anlık görüntünün anahtarını (tarih) alır.
                    DataSnapshot distanceSnapshot = snapshot.child("distance_amount"); // Anlık görüntüden mesafe verisi alanını alır.
                    if(distanceSnapshot.exists()){ // Eğer mesafe verisi alanı varsa
                        Float distance = distanceSnapshot.getValue(Float.class); // Mesafeyi alır.
                        if(distance != null){ // Eğer mesafe null değilse
                            entries.add(new BarEntry(i, distance)); // Grafik girdisine ekler.
                            labels.add(date); // X ekseni etiketine tarihi ekler.
                            totalDistance += distance; // Toplam mesafeye ekler.
                            validDistanceCount++; // Geçerli mesafe sayısını artırır.
                            i++; // Girdi sayacını artırır.
                        } else{
                            Log.e("DistanceHistory","distance_amount değeri null :" + date); // Eğer mesafe null ise hata mesajı yazdırır.
                        }
                    } else{
                        Log.e("DistanceHistory","distance_amount key değeri bulunamadı :" + date); // Eğer mesafe verisi alanı bulunamazsa hata mesajı yazdırır.
                    }
                }
                if (entries.isEmpty()) { // Eğer hiç veri girişi yoksa
                    noDataTextView.setVisibility(View.VISIBLE); // Veri yok metin alanını görünür yapar.
                    distanceChart.setVisibility(View.GONE); // Grafiği gizler.
                    totalDistanceTextView.setText("0 km"); // Toplam mesafe metin alanını sıfır olarak ayarlar.
                    averageDistanceTextView.setText("0 km"); // Ortalama mesafe metin alanını sıfır olarak ayarlar.
                    Log.e("DistanceHistory", "Firebase'de mesafe verisi bulunamadı."); // Hata mesajı yazdırır.
                } else { // Eğer veri girişi varsa
                    noDataTextView.setVisibility(View.GONE); // Veri yok metin alanını gizler.
                    distanceChart.setVisibility(View.VISIBLE); // Grafiği görünür yapar.
                    // Toplam ve ortalama mesafe hesapla
                    double averageDistance = validDistanceCount > 0 ? totalDistance / validDistanceCount : 0; // Ortalama mesafeyi hesaplar.
                    totalDistanceTextView.setText(String.format("%.2f km", totalDistance)); // Toplam mesafe metin alanını günceller.
                    averageDistanceTextView.setText(String.format("%.2f km", averageDistance)); // Ortalama mesafe metin alanını günceller.

                    BarDataSet dataSet = new BarDataSet(entries, "Distance (km)"); // Çubuk grafik veri setini oluşturur.
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Veri setine renk paleti uygular.
                    dataSet.setValueTextSize(12f); // Sütun üzerindeki değerlerin yazı boyutunu ayarlar.
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // Sütun üzerindeki değerlerin yazı kalınlığını ayarlar.
                    BarData barData = new BarData(dataSet); // Çubuk grafik verisini oluşturur.
                    // X Ekseni Ayarları
                    XAxis xAxis = distanceChart.getXAxis(); // X eksenini alır.
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // X ekseni etiketlerini biçimlendirir.
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X eksenini altta konumlandırır.
                    xAxis.setGranularity(1f); // Her bir girdi için bir etiket gösterir.
                    xAxis.setLabelRotationAngle(-45f); // Etiketleri 45 derece eğik gösterir.
                    xAxis.setTextSize(12f); // X ekseni yazı boyutunu ayarlar.
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD); // X ekseni yazı kalınlığını ayarlar.

                    // Y Ekseni Ayarları
                    YAxis yAxisLeft = distanceChart.getAxisLeft(); // Sol Y eksenini alır.
                    yAxisLeft.setAxisMinimum(0f); // Y ekseni minimum değerini ayarlar.
                    yAxisLeft.setGranularity(1f); // Y ekseni değerlerini tam sayı olarak gösterir.
                    yAxisLeft.setAxisLineColor(getResources().getColor(R.color.black)); // Y ekseni rengini siyah yapar.
                    yAxisLeft.setTextSize(12f); // Y ekseni yazı boyutunu ayarlar.
                    yAxisLeft.setTypeface(Typeface.DEFAULT_BOLD); // Y ekseni yazı kalınlığını ayarlar.

                    Legend legend = distanceChart.getLegend(); // Grafik göstergesini alır.
                    legend.setTextSize(14f); // Gösterge yazı boyutunu ayarlar.
                    legend.setTypeface(Typeface.DEFAULT_BOLD); // Gösterge yazı kalınlığını ayarlar.

                    //Grafik Ayarları
                    distanceChart.getAxisRight().setEnabled(false); // Sağ Y eksenini devre dışı bırakır.
                    distanceChart.getDescription().setEnabled(false); // Grafik açıklamasını devre dışı bırakır.
                    distanceChart.animateY(1000); // Y ekseninde animasyon uygular.
                    distanceChart.setDrawGridBackground(true); // Grid arka planını aktif yapar.
                    distanceChart.setGridBackgroundColor(getResources().getColor(R.color.light_grey)); // Grid arka plan rengini açık gri yapar.
                    distanceChart.setDrawBorders(true); // Grafiğe sınır ekler.
                    distanceChart.setBorderColor(getResources().getColor(R.color.black)); // Grafik sınır rengini siyah yapar.

                    distanceChart.setData(barData); // Grafiğe veriyi yükler.
                    distanceChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)); // X eksenini etiket formatını ayarlar.
                    distanceChart.getDescription().setEnabled(false); // Grafik açıklamasını devre dışı bırakır.
                    distanceChart.invalidate(); // Grafiği günceller.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DistanceHistory", "Veri okuma iptal edildi: ", error.toException()); // Hata durumunda hata mesajı yazdırır.
            }
        });
    }
}