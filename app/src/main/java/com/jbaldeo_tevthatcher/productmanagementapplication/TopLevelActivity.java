package com.jbaldeo_tevthatcher.productmanagementapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TopLevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void receiveStock(View view){
        Intent intent = new Intent(this, StockActivity.class);
        intent.putExtra("EXTRA_ACTIVITY", "receiveStock");
        startActivity(intent);
    }

    public void orderStock(View view){
        Intent intent = new Intent(this, StockActivity.class);
        intent.putExtra("EXTRA_ACTIVITY", "orderStock");
        startActivity(intent);
    }
}