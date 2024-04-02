package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.card.MaterialCardView;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        MaterialCardView view_results, manage_results;

        view_results = findViewById(R.id.view_results_card);
        manage_results = findViewById(R.id.manage_results_card);

        view_results.setOnClickListener(view -> startActivity(new Intent(ResultsActivity.this,ViewResultsActivity.class)));

        manage_results.setOnClickListener(view -> startActivity(new Intent(ResultsActivity.this, ManageResultsActivity.class)));


    }
}