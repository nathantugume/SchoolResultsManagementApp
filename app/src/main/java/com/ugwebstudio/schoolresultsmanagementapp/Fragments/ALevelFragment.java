package com.ugwebstudio.schoolresultsmanagementapp.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.GradingScale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ALevelFragment extends Fragment {

    private TableLayout tableLayout;
    private FirebaseFirestore db;

    private Context context;
    private  ShimmerFrameLayout shimmerContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_a_level, container, false);
        context = view.getContext();
        shimmerContainer = view.findViewById(R.id.shimmer_view_container);
        shimmerContainer.startShimmer();
        db = FirebaseFirestore.getInstance();
        tableLayout = view.findViewById(R.id.table_layout);

        fetchGradingScales();

        FloatingActionButton fab = view.findViewById(R.id.fab_open_dialog);
        fab.setOnClickListener(view2 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater2 = requireActivity().getLayoutInflater();
            View view1 = inflater2.inflate(R.layout.dialogue_add_grade, null);
            builder.setView(view1)
                    .setPositiveButton("Save", (dialog, id) -> {
                        // Handle the save action

                        saveGradingScale(view1);

                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        dialog.cancel();

                    });
            builder.create();
            builder.show();
        });


        return view;


    }

    public void saveGradingScale(View dialogView) {

        Log.d("grades","class reached");
        TextInputEditText fromEditText = dialogView.findViewById(R.id.fromEditText);
        TextInputEditText toEditText = dialogView.findViewById(R.id.toEditText);
        TextInputEditText gradeEditText = dialogView.findViewById(R.id.gradeEditText);

        String fromStr = fromEditText.getText().toString();
        String toStr = toEditText.getText().toString();
        String grade = gradeEditText.getText().toString().toUpperCase();

        // Sanitize and validate inputs
        if (!fromStr.isEmpty() && !toStr.isEmpty() && !grade.isEmpty()) {
            int from = Integer.parseInt(fromStr);
            int to = Integer.parseInt(toStr);

            if (from >= 0 && to >= from) { // Basic validation
                Map<String, Object> gradingScale = new HashMap<>();
                gradingScale.put("from", from);
                gradingScale.put("to", to);
                gradingScale.put("grade", grade);
                gradingScale.put("level","a_level");

                // Save gradingScale to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("gradingScales")
                        .add(gradingScale)
                        .addOnSuccessListener(documentReference -> Toast.makeText(context, "Grading Scale saved successfully.", Toast.LENGTH_SHORT).show()

                        )
                        .addOnFailureListener(e -> Toast.makeText(context, "Error saving Grading Scale.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Invalid range. 'From' must be less than or equal to 'To'.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchGradingScales() {
        shimmerContainer.startShimmer(); // Stop shimmer animation
        tableLayout.setVisibility(View.GONE);
        db.collection("gradingScales").whereEqualTo("level", "a_level")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<GradingScale> gradingScales = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GradingScale scale = document.toObject(GradingScale.class);
                            scale.setId(document.getId());
                            gradingScales.add(scale);
                        }
                        // Update your UI with the list of grading scales
                        shimmerContainer.stopShimmer(); // Stop shimmer animation
                        tableLayout.setVisibility(View.VISIBLE);
                        populateTableWithGrades(gradingScales);
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        Toast.makeText(context, "Failed to fetch grading scales.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    public void populateTableWithGrades(List<GradingScale> gradingScales) {


        for (GradingScale scale : gradingScales) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            TextView gradeText = new TextView(getContext());
            gradeText.setText(scale.getGrade());
            gradeText.setPadding(8, 8, 8, 8);

            TextView gradeEmptyText = new TextView(getContext());
            gradeText.setText("no grades added");
            gradeText.setPadding(8, 8, 8, 8);

            EditText fromEdit = new EditText(getContext());
            fromEdit.setText(String.valueOf(scale.getFrom()));
            fromEdit.setPadding(8, 8, 8, 8);

            EditText toEdit = new EditText(getContext());
            toEdit.setText(String.valueOf(scale.getTo()));
            toEdit.setPadding(8, 8, 8, 8);

            // Inside your loop where you create the table rows
            TextView deleteAction = new TextView(getContext());
            deleteAction.setText("Delete");
            deleteAction.setPadding(8, 8, 8, 8);
            deleteAction.setTextColor(Color.RED); // Set delete text color to red for visibility
            deleteAction.setOnClickListener(v -> {
                // Remove the row from the tableLayout
                tableLayout.removeView(tableRow);
                //  Implement further logic to remove the grade from your data source or Firestore
                String gradeId = scale.getId(); // Assuming GradingScale has an ID field that matches Firestore document ID
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("gradingScales").document(gradeId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            tableLayout.removeView(tableRow); // Remove the row from the UI
                            Toast.makeText(getContext(), "Grade deleted successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting grade", Toast.LENGTH_SHORT).show());
            });


            // Add views to the row
            tableRow.addView(gradeText);
            tableRow.addView(fromEdit);
            tableRow.addView(toEdit);
            tableRow.addView(deleteAction); // Add the delete action to your row


            // Add row to the table
            tableLayout.addView(tableRow);
        }
    }


}