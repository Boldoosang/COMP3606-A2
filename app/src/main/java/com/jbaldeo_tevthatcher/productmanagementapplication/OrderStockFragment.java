package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class OrderStockFragment extends Fragment {

    public OrderStockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getView();
        return inflater.inflate(R.layout.fragment_order_stock, container, false);
    }
}