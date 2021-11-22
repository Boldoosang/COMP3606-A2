package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class TopLevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void receiveStock(View view){
        Intent intent = new Intent(this, ReceiveStockActivity.class);
        startActivity(intent);
    }

    public void orderStock(View view){
        Intent intent = new Intent(this, OrderStockActivity.class);
        startActivity(intent);
    }

    public void syncStock(View view){
        new SyncProductsTask().execute();
    }

    //https://jbtt3607a2.free.beeceptor.com

    private class SyncProductsTask extends AsyncTask<Void, Void, String> {
        SQLiteOpenHelper productDatabaseHelper;
        SQLiteDatabase database;
        Cursor cursor;

        protected void onPreExecute() {
            productDatabaseHelper = new ProductDatabaseHelper(TopLevelActivity.this);
            database = productDatabaseHelper.getReadableDatabase();
        }

        protected String doInBackground(Void... v) {
            String selectQuery = "SELECT * FROM PRODUCT WHERE DIRTY = 1";
            ArrayList<HashMap<String, String>> dirtyStock;
            dirtyStock = new ArrayList<>();
            cursor = database.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<>();
                    int stockOnHand = cursor.getColumnIndex("STOCK_ON_HAND");
                    int stockInTransit = cursor.getColumnIndex("STOCK_IN_TRANSIT");

                    map.put("_id", cursor.getString(0));
                    map.put("StockOnHand", cursor.getString(stockOnHand));
                    map.put("StockInTransit", cursor.getString(stockInTransit));

                    dirtyStock.add(map);
                } while (cursor.moveToNext());
            }

            database.close();
            Gson gson = new GsonBuilder().create();

            //Use GSON to serialize Array List to JSON
            return gson.toJson(dirtyStock);
        }

        protected void onPostExecute(String res) {
            Toast.makeText(TopLevelActivity.this, "Attempting to sync...", Toast.LENGTH_LONG).show();
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("dirtyStock", res);
            client.post("https://jbtt3607a2.free.beeceptor.com/syncdb.php", params,new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String byteToString;
                    byteToString = new String(responseBody, StandardCharsets.UTF_8);
                    Toast.makeText(TopLevelActivity.this, "Successful response: " + byteToString, Toast.LENGTH_LONG).show();
                    new resetDirtyBit().execute();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    if(statusCode == 404){
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    }else if(statusCode == 500){
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                    }
                }
            });

            if(res == null) {
                Toast toast = Toast.makeText(TopLevelActivity.this, "Sync Stock Unsuccessful", Toast.LENGTH_SHORT);
                toast.show();
            }
            Toast.makeText(TopLevelActivity.this, "Sync post execute", Toast.LENGTH_LONG).show();
        }
    }

    private class resetDirtyBit extends AsyncTask<Void, Void, Boolean> {
        ContentValues updatedValues = new ContentValues();
        SQLiteOpenHelper productDatabaseHelper;
        SQLiteDatabase database;

        protected void onPreExecute() {
            productDatabaseHelper = new ProductDatabaseHelper(TopLevelActivity.this);
            database = productDatabaseHelper.getReadableDatabase();
            updatedValues.put("DIRTY", false);
        }

        protected Boolean doInBackground(Void... v) {
            String selectQuery = "SELECT * FROM PRODUCT WHERE DIRTY = 1";

            Cursor cursor = database.rawQuery(selectQuery, null);

            if(cursor.moveToFirst()){
                do {
                    database.update("PRODUCT", updatedValues, "_id = ?", new String[]{cursor.getString(0)});
                } while (cursor.moveToNext());
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if(!success) {
                Toast toast = Toast.makeText(TopLevelActivity.this, "Sync Drinks Unsuccessful", Toast.LENGTH_SHORT);
                toast.show();
            }
            Toast.makeText(TopLevelActivity.this, "Sync post execute", Toast.LENGTH_LONG).show();
        }
    }
}