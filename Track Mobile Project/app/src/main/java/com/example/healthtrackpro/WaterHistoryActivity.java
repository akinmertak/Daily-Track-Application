package com.example.healthtrackpro;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class WaterHistoryActivity extends AppCompatActivity {

    private BarChart waterChart;
    private DatabaseReference waterRef;
    private FirebaseAuth mAuth;
    private TextView totalWaterTextView;
    private TextView averageWaterTextView;
    private TextView noDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_history);

        waterChart = findViewById(R.id.waterChart);
        totalWaterTextView = findViewById(R.id.totalWaterTextView);
        averageWaterTextView = findViewById(R.id.averageWaterTextView);
        noDataTextView = findViewById(R.id.noDataTextView);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            waterRef = FirebaseDatabase.getInstance().getReference("water_history").child(userId);
            loadWaterData();
        }
    }

    private void loadWaterData() {
        waterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int i = 0;
                int totalWater = 0;
                int validWaterCount = 0;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String date = snapshot.getKey();
                    Integer water = snapshot.child("water_amount").getValue(Integer.class);
                    if (water != null){
                        entries.add(new BarEntry(i, water));
                        labels.add(date);
                        totalWater += water;
                        validWaterCount++;
                        i++;
                    } else {
                        Log.e("WaterHistory", "Su miktarı null. Tarih: " + date);
                    }
                }

                if (entries.isEmpty()) {
                    noDataTextView.setVisibility(View.VISIBLE);
                    waterChart.setVisibility(View.GONE);
                    totalWaterTextView.setText("0 ml");
                    averageWaterTextView.setText("0 ml");
                    Log.e("WaterHistory", "Firebase'de su verisi bulunamadı.");

                }else{
                    noDataTextView.setVisibility(View.GONE);
                    waterChart.setVisibility(View.VISIBLE);
                    // Toplam ve ortalama su tüketimini hesapla
                    int averageWater = validWaterCount > 0 ? totalWater / validWaterCount : 0;

                    totalWaterTextView.setText(String.format("%d ml", totalWater));
                    averageWaterTextView.setText(String.format("%d ml", averageWater));


                    BarDataSet dataSet = new BarDataSet(entries, "Water Intake (ml)");
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    dataSet.setValueTextSize(12f); // Sütun üzerindeki değerlerin yazı boyutu
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // Sütun üzerindeki değerlerin yazı kalınlığı
                    BarData barData = new BarData(dataSet);

                    // X Ekseni Ayarları
                    XAxis xAxis = waterChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f); // Her bir entry için bir etiket göstermesi
                    xAxis.setLabelRotationAngle(-45f);
                    xAxis.setTextSize(12f); // X ekseni yazı boyutu
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD); // X ekseni yazı kalınlığı// Etiketleri 45 derece eğik gösterme

                    // Y Ekseni Ayarları
                    YAxis yAxisLeft = waterChart.getAxisLeft();
                    yAxisLeft.setAxisMinimum(0f);
                    yAxisLeft.setGranularity(1f); // Y ekseni değerleri tam sayı olarak gösterilmesi için
                    yAxisLeft.setAxisLineColor(getResources().getColor(R.color.black)); //Y ekseni rengini siyah yaptık.
                    yAxisLeft.setTextSize(12f);
                    yAxisLeft.setTypeface(Typeface.DEFAULT_BOLD);


                    Legend legend = waterChart.getLegend();
                    legend.setTextSize(14f); // Başlık yazı boyutunu ayarlama

                    legend.setTypeface(Typeface.DEFAULT_BOLD);

                    //Grafik Ayarları
                    waterChart.getAxisRight().setEnabled(false); // Sağ y eksenini devre dışı bırak
                    waterChart.getDescription().setEnabled(false); // Açıklamayı devre dışı bırak
                    waterChart.animateY(1000); // Y ekseninde animasyon
                    waterChart.setDrawGridBackground(true); //Grid arka planı aktif.
                    waterChart.setGridBackgroundColor(getResources().getColor(R.color.light_grey)); // Grid arka plan rengini açık gri yaptık
                    waterChart.setDrawBorders(true); //Grafiğe sınır eklendi
                    waterChart.setBorderColor(getResources().getColor(R.color.black));//Grafik sınır rengi siyah.

                    waterChart.setData(barData);
                    waterChart.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WaterHistory", "onCancelled: " + error.getMessage());
            }
        });
    }
}