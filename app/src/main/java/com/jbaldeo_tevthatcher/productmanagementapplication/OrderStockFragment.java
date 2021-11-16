package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class OrderStockFragment extends Fragment implements View.OnClickListener{

    SQLiteDatabase db;
    Cursor cursor;

    private int spinnerIndex;

    public OrderStockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getView();
        return inflater.inflate(R.layout.fragment_ordering_stocks, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            OutputFragment outputFragment = new OutputFragment();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.outputContainer, outputFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.commit();
        } else {
            spinnerIndex = savedInstanceState.getInt("spinnerIndex");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        Button makeOrderButton = (Button) view.findViewById(R.id.orderingStock_btn);
        makeOrderButton.setOnClickListener(this);

        Spinner orderStockSpinner = (Spinner) view.findViewById(R.id.orderStockSpinner);
        ProductDatabaseHelper productDBHelper = new ProductDatabaseHelper(getContext());

        if (view != null) {
            try {
                db = productDBHelper.getReadableDatabase();
                cursor = db.query("PRODUCT", new String[]{"_id", "NAME"}, null, null, null, null, null);

                ArrayList<String> productNames = new ArrayList<>();

                if(cursor.moveToFirst()){
                    productNames.add(cursor.getString(1));

                    while(cursor.moveToNext())
                        productNames.add(cursor.getString(1));

                } else {
                    productNames.add("No products available");
                    orderStockSpinner.setEnabled(false);
                }


                ArrayAdapter spinnerAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, productNames);

                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                orderStockSpinner.setAdapter(spinnerAdapter);
                orderStockSpinner.setSelection(spinnerIndex);
            } catch(SQLException e){
                Toast toast = Toast.makeText(getContext(), "Unable to access database: " + e.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
                orderStockSpinner.setEnabled(false);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("spinnerIndex", spinnerIndex);
    }


    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.orderingStock_btn:
                makeOrder();
        }
    }

    public void makeOrder(){
        View v = getView();
        Spinner orderStockSpinner = (Spinner) v.findViewById(R.id.orderStockSpinner);
        TextView orderTextView = (TextView) v.findViewById(R.id.editTextNumber);

        String orderText = orderTextView.getText().toString();
        String spinnerText = orderStockSpinner.getSelectedItem().toString();


        ProductDatabaseHelper productDBHelper = new ProductDatabaseHelper(getContext());
        ContentValues updatedStock = new ContentValues();


        try{

            db = productDBHelper.getReadableDatabase();
            cursor = db.rawQuery("select STOCK_IN_TRANSIT from PRODUCT where NAME = '" + spinnerText + "'", null);
            cursor.moveToFirst();

            int numberInTransit = Integer.parseInt(orderText) + cursor.getInt(0);

            updatedStock.put("STOCK_IN_TRANSIT", numberInTransit);
            db.update("PRODUCT", updatedStock, "NAME = ?", new String[]{spinnerText});

        } catch(SQLException e){
            Toast toast = Toast.makeText(getContext(), "Unable to access database: " + e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            orderStockSpinner.setEnabled(false);
        }
    }
}