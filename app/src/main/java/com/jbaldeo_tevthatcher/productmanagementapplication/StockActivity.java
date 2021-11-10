package com.jbaldeo_tevthatcher.productmanagementapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

public class StockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        Intent intent = getIntent();

        String activityType = intent.getExtras().getString("EXTRA_ACTIVITY");

        if(savedInstanceState == null){
            if(activityType.equals("receiveStock")) {
                ReceiveStockFragment receiveStockFragment = new ReceiveStockFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activityContainer, receiveStockFragment);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();
            } else {
                OrderStockFragment orderStockFragment = new OrderStockFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activityContainer, orderStockFragment);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();
            }
        }

    }
}