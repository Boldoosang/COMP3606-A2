package com.jbaldeo_tevthatcher.productmanagementapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OutputFragment extends Fragment {

    Cursor cursor;
    SQLiteDatabase db;

    public OutputFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_output, container, false);
    }

    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }
}