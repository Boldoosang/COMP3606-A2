package com.jbaldeo_tevthatcher.productmanagementapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class StockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        Intent intent = getIntent();
        String activityType = intent.getExtras().getString("EXTRA_ACTIVITY");
        System.out.println(activityType);

    }
}