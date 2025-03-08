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

public class StepsHistoryActivity extends AppCompatActivity {
    private BarChart stepsChart;
    private DatabaseReference stepsRef;
    private FirebaseAuth mAuth;
    private TextView noDataTextView;
    private LinearLayout rootLayout;
    private TextView totalStepsTextView;
    private TextView averageStepsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_history);

        stepsChart = findViewById(R.id.stepsChart);
        noDataTextView = findViewById(R.id.noDataTextView);
        rootLayout = findViewById(R.id.rootLayout);
        totalStepsTextView = findViewById(R.id.totalStepsTextView);
        averageStepsTextView = findViewById(R.id.averageStepsTextView);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            stepsRef = FirebaseDatabase.getInstance().getReference("steps_history").child(userId);
            loadStepsData();
        }


    }

    private void loadStepsData() {
        stepsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int i = 0;
                int totalSteps = 0;
                int validStepsCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.getKey();
                    DataSnapshot stepsSnapshot = snapshot.child("step_amount");
                    if (stepsSnapshot.exists()){
                        Integer steps = stepsSnapshot.getValue(Integer.class);
                        if(steps != null){
                            entries.add(new BarEntry(i, steps));
                            labels.add(date);
                            totalSteps += steps;
                            validStepsCount++;
                            i++;
                        }
                        else{
                            Log.e("StepsHistory","step_amount değeri null :" + date);
                        }
                    }
                    else{
                        Log.e("StepsHistory","step_amount key değeri bulunamadı :" + date);
                    }
                }
                if (entries.isEmpty()) {
                    noDataTextView.setVisibility(View.VISIBLE);
                    stepsChart.setVisibility(View.GONE);
                    totalStepsTextView.setText("0");
                    averageStepsTextView.setText("0");
                    Log.e("StepsHistory", "Firebase'de adım verisi bulunamadı.");
                } else {
                    noDataTextView.setVisibility(View.GONE);
                    stepsChart.setVisibility(View.VISIBLE);
                    // Toplam ve ortalama adım sayısını hesapla
                    int averageSteps = validStepsCount > 0 ? totalSteps / validStepsCount : 0;
                    totalStepsTextView.setText(String.valueOf(totalSteps));
                    averageStepsTextView.setText(String.valueOf(averageSteps));
                    BarDataSet dataSet = new BarDataSet(entries, "Steps Amount");
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                    dataSet.setValueTextSize(12f);
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

                    BarData barData = new BarData(dataSet);


                    // X Ekseni Ayarları
                    XAxis xAxis = stepsChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f); // Her bir entry için bir etiket göstermesi
                    xAxis.setLabelRotationAngle(-45f);
                    xAxis.setTextSize(12f); // X ekseni yazı boyutu
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD);// X ekseni yazı kalınlığı// Etiketleri 45 derece eğik gösterme

                    // Y Ekseni Ayarları
                    YAxis yAxisLeft = stepsChart.getAxisLeft();
                    yAxisLeft.setAxisMinimum(0f);
                    yAxisLeft.setGranularity(1f); // Y ekseni değerleri tam sayı olarak gösterilmesi için
                    yAxisLeft.setAxisLineColor(getResources().getColor(R.color.black)); //Y ekseni rengini siyah yaptık.
                    yAxisLeft.setTextSize(12f);
                    yAxisLeft.setTypeface(Typeface.DEFAULT_BOLD);


                    Legend legend = stepsChart.getLegend();
                    legend.setTextSize(14f); // Başlık yazı boyutunu ayarlama
                    legend.setTypeface(Typeface.DEFAULT_BOLD);

                    //Grafik Ayarları
                    stepsChart.getAxisRight().setEnabled(false); // Sağ y eksenini devre dışı bırak
                    stepsChart.getDescription().setEnabled(false); // Açıklamayı devre dışı bırak
                    stepsChart.animateY(1000); // Y ekseninde animasyon
                    stepsChart.setDrawGridBackground(true); //Grid arka planı aktif.
                    stepsChart.setGridBackgroundColor(getResources().getColor(R.color.light_grey)); // Grid arka plan rengini açık gri yaptık
                    stepsChart.setDrawBorders(true); //Grafiğe sınır eklendi
                    stepsChart.setBorderColor(getResources().getColor(R.color.black));//Grafik sınır rengi siyah.

                    stepsChart.setData(barData);
                    stepsChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
                    stepsChart.getDescription().setEnabled(false);
                    stepsChart.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StepsHistory", "Veri okuma iptal edildi: ", error.toException());
            }
        });
    }
}