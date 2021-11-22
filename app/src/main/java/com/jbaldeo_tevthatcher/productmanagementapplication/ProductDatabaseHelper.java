package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "PRODUCT_DB";
    private static final int DB_VER = 2;

    ProductDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, DB_VER);

        insertProduct(db, "Samsung Galaxy S21", 5, 5, 20, 5, 5);
        insertProduct(db, "iPhone 13 Pro Max", 5, 5, 20, 5, 5);
        insertProduct(db, "Google Pixel 6", 5, 5, 20, 5, 5);
        insertProduct(db, "OnePlus 10", 5, 5, 20, 5, 5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        updateDatabase(db, oldVer, newVer);
    }

    public void updateDatabase(SQLiteDatabase db, int oldVer, int newVer){
        if(oldVer < 1){
            db.execSQL("CREATE TABLE PRODUCT (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, STOCK_ON_HAND INTEGER, STOCK_IN_TRANSIT INTEGER, PRICE FLOAT, REORDER_QUANTITY INTEGER, REORDER_AMOUNT INTEGER)");
        }
        if(oldVer < 3){
            db.execSQL("ALTER TABLE PRODUCT ADD COLUMN DIRTY default 'FALSE'");
        }
    }

    public static void insertProduct(SQLiteDatabase db, String name, int StockOnHand, int StockInTransit, double Price, int ReorderQuantity, int ReorderAmount){
        ContentValues productValues = new ContentValues();
        productValues.put("NAME", name);
        productValues.put("STOCK_ON_HAND", StockOnHand);
        productValues.put("STOCK_IN_TRANSIT", StockInTransit);
        productValues.put("PRICE", Price);
        productValues.put("REORDER_QUANTITY", ReorderQuantity);
        productValues.put("REORDER_AMOUNT", ReorderAmount);
        db.insert("PRODUCT", null, productValues);
    }

}