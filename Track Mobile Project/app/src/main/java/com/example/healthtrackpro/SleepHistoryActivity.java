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

public class SleepHistoryActivity extends AppCompatActivity {

    private BarChart sleepChart;
    private DatabaseReference sleepRef;
    private FirebaseAuth mAuth;
    private TextView noDataTextView;
    private LinearLayout rootLayout;
    private TextView totalSleepTimeTextView;
    private TextView averageSleepTimeTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_history);

        sleepChart = findViewById(R.id.sleepChart);
        noDataTextView = findViewById(R.id.noDataTextView);
        rootLayout = findViewById(R.id.rootLayout);
        totalSleepTimeTextView = findViewById(R.id.totalSleepTimeTextView);
        averageSleepTimeTextView = findViewById(R.id.averageSleepTimeTextView);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            sleepRef = FirebaseDatabase.getInstance().getReference("sleep_history").child(userId);
            loadSleepData();
        }

    }

    private void loadSleepData() {
        sleepRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int i = 0;
                double totalSleep = 0;
                int validSleepCount = 0;



                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.getKey();
                    DataSnapshot sleepSnapshot = snapshot.child("sleep_amount");
                    if (sleepSnapshot.exists()){
                        Float sleep = sleepSnapshot.getValue(Float.class);
                        if(sleep != null){
                            entries.add(new BarEntry(i, sleep));
                            labels.add(date);
                            totalSleep += sleep;
                            validSleepCount++;
                            i++;
                        }else{
                            Log.e("SleepHistory","sleep_amount değeri null :" + date);
                        }
                    }else{
                        Log.e("SleepHistory","sleep_amount key değeri bulunamadı :" + date);
                    }


                }
                if (entries.isEmpty()) {
                    noDataTextView.setVisibility(View.VISIBLE);
                    sleepChart.setVisibility(View.GONE);
                    totalSleepTimeTextView.setText("0 hours");
                    averageSleepTimeTextView.setText("0 hours");
                    Log.e("SleepHistory", "Firebase'de uyku verisi bulunamadı.");

                } else {
                    noDataTextView.setVisibility(View.GONE);
                    sleepChart.setVisibility(View.VISIBLE);

                    // Toplam ve ortalama uyku süresini hesapla
                    double averageSleep = validSleepCount > 0 ? totalSleep / validSleepCount : 0;
                    totalSleepTimeTextView.setText(String.format("%.2f hours", totalSleep));
                    averageSleepTimeTextView.setText(String.format("%.2f hours", averageSleep));


                    BarDataSet dataSet = new BarDataSet(entries, "Sleep Hours");
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    dataSet.setValueTextSize(12f);
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                    BarData barData = new BarData(dataSet);

                    // X Ekseni Ayarları
                    XAxis xAxis = sleepChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f); // Her bir entry için bir etiket göstermesi
                    xAxis.setLabelRotationAngle(-45f);
                    xAxis.setTextSize(12f); // X ekseni yazı boyutu
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD);// X ekseni yazı kalınlığı// Etiketleri 45 derece eğik gösterme

                    YAxis yAxisLeft = sleepChart.getAxisLeft();
                    yAxisLeft.setAxisMinimum(0f);
                    yAxisLeft.setGranularity(1f); // Y ekseni değerleri tam sayı olarak gösterilmesi için
                    yAxisLeft.setAxisLineColor(getResources().getColor(R.color.black)); //Y ekseni rengini siyah yaptık.
                    yAxisLeft.setTextSize(12f);
                    yAxisLeft.setTypeface(Typeface.DEFAULT_BOLD);


                    Legend legend = sleepChart.getLegend();
                    legend.setTextSize(14f); // Başlık yazı boyutunu ayarlama

                    legend.setTypeface(Typeface.DEFAULT_BOLD);

                    //Grafik Ayarları
                    sleepChart.getAxisRight().setEnabled(false); // Sağ y eksenini devre dışı bırak
                    sleepChart.getDescription().setEnabled(false); // Açıklamayı devre dışı bırak
                    sleepChart.animateY(1000); // Y ekseninde animasyon
                    sleepChart.setDrawGridBackground(true); //Grid arka planı aktif.
                    sleepChart.setGridBackgroundColor(getResources().getColor(R.color.light_grey)); // Grid arka plan rengini açık gri yaptık
                    sleepChart.setDrawBorders(true); //Grafiğe sınır eklendi
                    sleepChart.setBorderColor(getResources().getColor(R.color.black));//Grafik sınır rengi siyah.

                    sleepChart.setData(barData);
                    sleepChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
                    sleepChart.getDescription().setEnabled(false);
                    sleepChart.invalidate();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SleepHistory", "Veri okuma iptal edildi: ", error.toException());
            }
        });
    }
}