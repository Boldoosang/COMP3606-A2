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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ReceiveStockFragment extends Fragment implements View.OnClickListener{

    SQLiteDatabase db;
    Cursor cursor;

    private int spinnerIndex;

    public ReceiveStockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // breaks when rotating

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
        Button updateStocksButton = (Button) view.findViewById(R.id.button);
        updateStocksButton.setOnClickListener(this);

        Spinner receiveStockSpinner = (Spinner) view.findViewById(R.id.receiveStockSpinner);
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
                    receiveStockSpinner.setEnabled(false);
                }


                ArrayAdapter spinnerAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, productNames);

                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                receiveStockSpinner.setAdapter(spinnerAdapter);
                receiveStockSpinner.setSelection(spinnerIndex);
            } catch(SQLException e){
                Toast toast = Toast.makeText(getContext(), "Unable to access database: " + e.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
                receiveStockSpinner.setEnabled(false);
            }
        }
    }

    //spinner.getSelectedItemPosition();
    //^Use this for onItemSelection
    //spinner.setSelection(spinnerIndex);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_receive_stock, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("spinnerIndex", spinnerIndex);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button:
                makeOrder();
        }
    }

    public void makeOrder(){
        View v = getView();
        Spinner receiveStockSpinner = (Spinner) v.findViewById(R.id.receiveStockSpinner);
        TextView updateStockTextView = (TextView) v.findViewById(R.id.editTextNumber);

        String orderText = updateStockTextView.getText().toString();
        String spinnerText = receiveStockSpinner.getSelectedItem().toString();


        ProductDatabaseHelper productDBHelper = new ProductDatabaseHelper(getContext());
        ContentValues updatedStock = new ContentValues();


        try{
            db = productDBHelper.getReadableDatabase();
            cursor = db.rawQuery("select STOCK_IN_TRANSIT from PRODUCT where NAME = '" + spinnerText + "'", null);
            cursor.moveToFirst();
            int numberInTransit = cursor.getInt(0) - Integer.parseInt(orderText);

            cursor = db.rawQuery("select STOCK_ON_HAND from PRODUCT where NAME = '" + spinnerText + "'", null);
            cursor.moveToFirst();
            int stockOnHand = Integer.parseInt(orderText) + cursor.getInt(0);

            updatedStock.put("STOCK_IN_TRANSIT", numberInTransit);
            updatedStock.put("STOCK_ON_HAND", stockOnHand);

            db.update("PRODUCT", updatedStock, "NAME = ?", new String[]{spinnerText});

        } catch(SQLException e){
            Toast toast = Toast.makeText(getContext(), "Unable to access database: " + e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            receiveStockSpinner.setEnabled(false);
        }
    }

    /*
    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }
    */
}