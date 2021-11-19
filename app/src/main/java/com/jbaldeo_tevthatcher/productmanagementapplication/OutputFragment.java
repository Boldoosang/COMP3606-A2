package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class OutputFragment extends Fragment {

    //Cursor cursor;
    //SQLiteDatabase db;

    public OutputFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_output, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();
        new PopulateSpinner().execute();
    }

    public void addProductToView(Cursor cursor){
        View view = getView();
        LinearLayout layoutOutputContainer = view.findViewById(R.id.outputViewLinearLayout);

        LinearLayout outputContainer = new LinearLayout(getContext());
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,0,50);
        outputContainer.setOrientation(LinearLayout.VERTICAL);
        outputContainer.setLayoutParams(layoutParams);


        TextView productName = new TextView(getContext());
        TextView stockOnHand = new TextView(getContext());
        TextView stockInTransit = new TextView(getContext());
        TextView reorderQuantity = new TextView(getContext());
        TextView reorderAmount = new TextView(getContext());
        TextView valuation = new TextView(getContext());
        TextView intransitValuation = new TextView(getContext());


        productName.setText(cursor.getString(0));
        productName.setTextColor(Color.BLACK);
        stockOnHand.setText("Stock on Hand: " + cursor.getString(1));
        stockOnHand.setTextColor(Color.BLACK);
        stockInTransit.setText("Stock in Transit: " + cursor.getString(2));
        stockInTransit.setTextColor(Color.BLACK);
        reorderQuantity.setText("Reorder Quantity: " + cursor.getString(4));
        reorderQuantity.setTextColor(Color.BLACK);
        reorderAmount.setText("Reorder Amount: " + cursor.getString(5));
        reorderAmount.setTextColor(Color.BLACK);

        double valuationPrice = cursor.getFloat(4) * cursor.getInt(1);
        double transitValuationPrice = cursor.getFloat(4) * cursor.getInt(2);

        valuation.setText("Valuation: $" + Double.toString(valuationPrice));
        valuation.setTextColor(Color.BLACK);
        intransitValuation.setText("In Transit Valuation: $" + Double.toString(transitValuationPrice));
        intransitValuation.setTextColor(Color.BLACK);

        productName.setTypeface(Typeface.DEFAULT_BOLD);
        productName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        productName.setTextColor(Color.BLACK);

        outputContainer.addView(productName);
        outputContainer.addView(stockOnHand);
        outputContainer.addView(stockInTransit);
        outputContainer.addView(reorderQuantity);
        outputContainer.addView(reorderAmount);
        outputContainer.addView(valuation);
        outputContainer.addView(intransitValuation);

        layoutOutputContainer.addView(outputContainer);
    }

    public void onDestroy(){
        super.onDestroy();
        //cursor.close();
        //db.close();
    }

    private class PopulateSpinner extends AsyncTask<Void, Void, Cursor> {
        ProductDatabaseHelper productDBHelper;
        View view;
        Cursor cursor;
        protected void onPreExecute(){
            productDBHelper = new ProductDatabaseHelper(getContext());
            view = getView();
        }

        protected Cursor doInBackground(Void... v){
            try {
                SQLiteDatabase db = productDBHelper.getReadableDatabase();
                cursor = db.query("PRODUCT", new String[] {"NAME", "STOCK_ON_HAND", "STOCK_IN_TRANSIT", "PRICE", "REORDER_QUANTITY", "REORDER_AMOUNT"}, null, null, null, null, "NAME ASC");

                return cursor;

            } catch(SQLException e){
                return null;
            }
        }

        protected void onPostExecute(Cursor res){
            if(res == null){
                Toast toast = Toast.makeText(view.getContext(), "Database Unavailable.", Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                if(res.moveToFirst()){
                    addProductToView(res);

                    while(res.moveToNext()){
                        addProductToView(res);
                    }
                }
            }
        }
    }
}