package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class OrderStockFragment extends Fragment implements View.OnClickListener{

    SQLiteDatabase db;
    Cursor cursor;
    private Spinner orderStockSpinner;

    private int spinnerIndex;

    public OrderStockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        new PopulateSpinner().execute();

        orderStockSpinner = view.findViewById(R.id.orderStockSpinner);
        orderStockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                new SelectedProduct().execute();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("spinnerIndex", orderStockSpinner.getSelectedItemPosition());
    }


    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.orderingStock_btn:
                new MakeOrder().execute();
                updateUI();
        }
    }

    public void updateUI(){
        OutputFragment outputFragment = new OutputFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.outputContainer, outputFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    private class MakeOrder extends AsyncTask<Void , Void, Boolean> {
        ContentValues updatedStock = new ContentValues();
        String orderText;
        String spinnerText;

        protected void onPreExecute(){
            View v = getView();
            Spinner orderStockSpinner = (Spinner) v.findViewById(R.id.orderStockSpinner);
            TextView orderTextView = (TextView) v.findViewById(R.id.editTextNumber);

            orderText = orderTextView.getText().toString();
            spinnerText = orderStockSpinner.getSelectedItem().toString();

        }

        @SuppressLint("WrongThread")
        protected Boolean doInBackground(Void... v){
            ProductDatabaseHelper productDBHelper = new ProductDatabaseHelper(getContext());

            if(orderText.equals(""))
                return false;

            try{
                db = productDBHelper.getReadableDatabase();
                cursor = db.rawQuery("select STOCK_IN_TRANSIT from PRODUCT where NAME = '" + spinnerText + "'", null);
                cursor.moveToFirst();

                int numberInTransit = Integer.parseInt(orderText) + cursor.getInt(0);

                updatedStock.put("STOCK_IN_TRANSIT", numberInTransit);
                db.update("PRODUCT", updatedStock, "NAME = ?", new String[]{spinnerText});
                return true;
            } catch(SQLException e){
                return false;
            }
        }

        protected void onPostExecute(Boolean result){
            new SelectedProduct().execute();
            if(orderText.equals("")){
                Toast toast = Toast.makeText(getContext(), "Invalid product details entered.", Toast.LENGTH_SHORT);
                toast.show();
            }
            if(!result){
                Toast toast = Toast.makeText(getContext(), "Unable to access database.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private class PopulateSpinner extends AsyncTask<Void, Void, Cursor>{

        Spinner orderStockSpinner;
        ProductDatabaseHelper productDBHelper;
        View view;

        protected void onPreExecute(){
            view = getView();
            orderStockSpinner = (Spinner) view.findViewById(R.id.orderStockSpinner);
            productDBHelper = new ProductDatabaseHelper(getContext());
        }

        protected Cursor doInBackground(Void... v){
            if (view != null) {
                try {
                    db = productDBHelper.getReadableDatabase();
                    cursor = db.query("PRODUCT", new String[]{"_id", "NAME"}, null, null, null, null, null);

                    return cursor;
                } catch(SQLException e){
                    return null;
                }
            }
            return null;
        }

        protected void onPostExecute(Cursor cursor){
            ArrayList<String> productNames = new ArrayList<>();

            if(cursor.moveToFirst()){
                productNames.add(cursor.getString(1));

                while(cursor.moveToNext())
                    productNames.add(cursor.getString(1));

                ArrayAdapter spinnerAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, productNames);

                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                orderStockSpinner.setAdapter(spinnerAdapter);

                orderStockSpinner.setSelection(spinnerIndex);
            } else {
                productNames.add("No products available");
                orderStockSpinner.setEnabled(false);
            }
        }
    }

    private class SelectedProduct extends AsyncTask<Void, Void, Cursor>{
        ProductDatabaseHelper productDBHelper;
        View view;
        protected void onPreExecute(){
            view = getView();
            productDBHelper = new ProductDatabaseHelper(getContext());
        }

        protected Cursor doInBackground(Void... v){
            if (view != null) {
                try {
                    Spinner orderSpinner = view.findViewById(R.id.orderStockSpinner);
                    String selectedProduct = orderSpinner.getSelectedItem().toString();
                    db = productDBHelper.getReadableDatabase();
                    cursor = db.query("PRODUCT", new String[]{"_id", "NAME", "STOCK_ON_HAND", "STOCK_IN_TRANSIT", "REORDER_QUANTITY", "REORDER_AMOUNT"}, "NAME = ?", new String[]{selectedProduct}, null, null, null);
                    return cursor;
                } catch(SQLException e){
                    return null;
                }
            }
            return null;
        }

        protected void onPostExecute(Cursor cursor){
            View view = getView();
            LinearLayout currentProductDetails = view.findViewById(R.id.currentProductDetails);

            try {
                currentProductDetails.removeAllViews();
            } catch(Throwable e){
                System.out.println(e.getMessage());
            }

            LinearLayout outputContainer = new LinearLayout(getContext());
            LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,16,0,0);
            outputContainer.setOrientation(LinearLayout.VERTICAL);
            outputContainer.setLayoutParams(layoutParams);

            if(cursor.moveToFirst()){
                //cursor.moveToPosition(spinnerIndex);

                TextView productName = new TextView(getContext());
                productName.setText("Product Name: " + cursor.getString(1));
                productName.setTextColor(Color.BLACK);

                TextView stockOnHand = new TextView(getContext());
                stockOnHand.setText("Stock on Hand: " + cursor.getString(2));
                stockOnHand.setTextColor(Color.BLACK);

                TextView stockInTransit = new TextView(getContext());
                stockInTransit.setText("Stock in Transit: " + cursor.getString(3));
                stockInTransit.setTextColor(Color.BLACK);

                TextView reorderQuantity = new TextView(getContext());
                reorderQuantity.setText("Reorder Quantity: " + cursor.getString(4));
                reorderQuantity.setTextColor(Color.BLACK);

                TextView reorderAmount = new TextView(getContext());
                reorderAmount.setText("Reorder Amount: " + cursor.getString(5));
                reorderAmount.setTextColor(Color.BLACK);


                outputContainer.addView(productName);
                outputContainer.addView(stockOnHand);
                outputContainer.addView(stockInTransit);
                outputContainer.addView(reorderQuantity);
                outputContainer.addView(reorderAmount);

                currentProductDetails.addView(outputContainer);
            } else {
                TextView error = new TextView(getContext());
                error.setText("No Product Selected");
                error.setTextColor(Color.BLACK);

                outputContainer.addView(error);
                currentProductDetails.addView(outputContainer);
            }
        }
    }
}