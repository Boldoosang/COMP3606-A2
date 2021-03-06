package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class ReceiveStockFragment extends Fragment implements View.OnClickListener{

    SQLiteDatabase db;
    Cursor cursor;
    private Spinner receiveStockSpinner;

    private int spinnerIndex;

    public ReceiveStockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        Button updateStocksButton = (Button) view.findViewById(R.id.button);
        updateStocksButton.setOnClickListener(this);

        new PopulateSpinner().execute();

        receiveStockSpinner = view.findViewById(R.id.receiveStockSpinner);;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receive_stock, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("spinnerIndex", receiveStockSpinner.getSelectedItemPosition());
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button:
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
            receiveStockSpinner = (Spinner) v.findViewById(R.id.receiveStockSpinner);
            TextView updateStockTextView = (TextView) v.findViewById(R.id.editTextNumber);

            orderText = updateStockTextView.getText().toString();
            spinnerText = receiveStockSpinner.getSelectedItem().toString();
        }

        protected Boolean doInBackground(Void... v){
            ProductDatabaseHelper productDBHelper = new ProductDatabaseHelper(getContext());

            if(orderText.equals(""))
                return false;

            try{
                db = productDBHelper.getReadableDatabase();
                cursor = db.rawQuery("select STOCK_IN_TRANSIT from PRODUCT where NAME = '" + spinnerText + "'", null);
                cursor.moveToFirst();

                int numberInTransit = cursor.getInt(0) - Integer.parseInt(orderText);

                if(numberInTransit < 0)
                    return false;

                cursor = db.rawQuery("select STOCK_ON_HAND from PRODUCT where NAME = '" + spinnerText + "'", null);
                cursor.moveToFirst();

                int stockOnHand = Integer.parseInt(orderText) + cursor.getInt(0);

                updatedStock.put("STOCK_IN_TRANSIT", numberInTransit);
                updatedStock.put("STOCK_ON_HAND", stockOnHand);
                updatedStock.put("DIRTY", true);

                db.update("PRODUCT", updatedStock, "NAME = ?", new String[]{spinnerText});

                return true;
            } catch(SQLException e){
                return false;
            }
        }

        protected void onPostExecute(Boolean result){
            if(orderText.equals("")){
                Toast toast = Toast.makeText(getContext(), "Invalid product quantity entered.", Toast.LENGTH_SHORT);
                toast.show();
            }
            if(!result){
                Toast toast = Toast.makeText(getContext(), "Unable to update database. Ensure quantity does not result in negative number.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private class PopulateSpinner extends AsyncTask<Void, Void, ArrayList<String>>{
        View view;
        Spinner receiveStockSpinner;
        ProductDatabaseHelper productDBHelper;

        protected void onPreExecute(){
            view = getView();
            receiveStockSpinner = (Spinner) view.findViewById(R.id.receiveStockSpinner);
            productDBHelper = new ProductDatabaseHelper(getContext());
        }

        protected ArrayList<String> doInBackground(Void... v){
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
                    return productNames;
                } catch(SQLException e){
                    return null;
                }
            }
            return null;
        }

        protected void onPostExecute(ArrayList<String> productNames){
            ArrayAdapter spinnerAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, productNames);

            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            receiveStockSpinner.setAdapter(spinnerAdapter);
            receiveStockSpinner.setSelection(spinnerIndex);
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }

}