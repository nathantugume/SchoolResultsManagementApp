package com.ugwebstudio.schoolresultsmanagementapp.teacher;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.InputFilterMinMax;
import com.ugwebstudio.schoolresultsmanagementapp.classes.SubjectResult;

import java.util.ArrayList;
import java.util.List;

public class ManageResultsActivity extends AppCompatActivity {

    private Spinner spinnerYear, spinnerClass, spinnerTerm, spinnerStudentName;
    private TableLayout tableLayoutSubjects;
    private FirebaseFirestore db;
    private ExtendedFloatingActionButton buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_results);

        spinnerYear = findViewById(R.id.year);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerTerm = findViewById(R.id.spinnerTerm);
        spinnerStudentName = findViewById(R.id.spinnerStudentName);
        tableLayoutSubjects = findViewById(R.id.tableLayoutSubjects);
        buttonSave = findViewById(R.id.buttonAddResult);

        buttonSave.setOnClickListener(view -> saveResultsToFirestore());

        db = FirebaseFirestore.getInstance();

        setupSpinners();
        loadSubjects();
    }

    private void setupSpinners() {
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.year_array));
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.classes_array));
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.terms_array));
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTerm.setAdapter(termAdapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSubjects() {
        String[] subjects = getResources().getStringArray(R.array.o_level_subs);
        int studentCount = 10;

        for (String subject : subjects) {
            TableRow row = new TableRow(this);
            TextView subjectText = new TextView(this);
            subjectText.setText(subject.length() >= 3 ? subject.substring(0, 3) : subject);
            row.addView(subjectText);

            double[] subjectScores = new double[studentCount];
            for (int i = 0; i < studentCount; i++) {
                EditText editText = new EditText(this);
                editText.setHint(" ");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                final int index = i;
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String input = s.toString();
                        if (!TextUtils.isEmpty(input) && input.matches("^\\d*(\\.\\d{0,1})?$")) {
                            double value = Double.parseDouble(input);
                            if (value >= 0.9 && value <= 3.0) {
                                subjectScores[index] = value;
                            } else {
                                subjectScores[index] = 0.0;
                                editText.setError("Value must be between 0.9 and 3.0");
                                Toast.makeText(ManageResultsActivity.this, "Value must be between 0.9 and 3.0", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            subjectScores[index] = 0.0;
                            editText.setError("Invalid format");
                            Toast.makeText(ManageResultsActivity.this, "Invalid format", Toast.LENGTH_SHORT).show();
                        }
                        updateTotalAndAverage(row, subjectScores);
                    }
                });

                row.addView(editText);
            }

            tableLayoutSubjects.addView(row);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateTotalAndAverage(TableRow row, double[] scores) {
        double total = 0;
        for (double score : scores) {
            total += score;
        }

        TextView totalView = new TextView(this);
        totalView.setText(String.format("%.1f", total));

        double average = total / scores.length;
        TextView averageView = new TextView(this);
        averageView.setText(String.format("%.1f", average));

        row.removeViewAt(row.getChildCount() - 1);
        row.removeViewAt(row.getChildCount() - 1);

        row.addView(totalView);
        row.addView(averageView);
    }

    private void fetchStudents() {
        String selectedClass = spinnerClass.getSelectedItem() != null ? spinnerClass.getSelectedItem().toString() : "";
        String selectedYear = spinnerYear.getSelectedItem() != null ? spinnerYear.getSelectedItem().toString() : "";

        if (TextUtils.isEmpty(selectedClass) || TextUtils.isEmpty(selectedYear)) {
            Toast.makeText(this, "Please select both year and class", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading students...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("students")
                .whereEqualTo("studentClass", selectedClass)
                .whereEqualTo("academicYear", selectedYear)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        List<String> studentNames = new ArrayList<>();
                        List<String> studentIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            studentNames.add(document.getString("name"));
                            studentIds.add(document.getId());
                        }
                        updateStudentSpinner(studentNames, studentIds);
                    } else {
                        Toast.makeText(ManageResultsActivity.this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStudentSpinner(List<String> studentNames, List<String> studentIds) {
        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudentName.setAdapter(studentAdapter);
        spinnerStudentName.setTag(studentIds);
    }

    private void saveResultsToFirestore() {
        String selectedYear = sanitizeSpinnerValue(spinnerYear.getSelectedItem() != null ? spinnerYear.getSelectedItem().toString() : "");
        String selectedClass = sanitizeSpinnerValue(spinnerClass.getSelectedItem() != null ? spinnerClass.getSelectedItem().toString() : "");
        String selectedTerm = sanitizeSpinnerValue(spinnerTerm.getSelectedItem() != null ? spinnerTerm.getSelectedItem().toString() : "");
        String selectedStudentName = sanitizeSpinnerValue(spinnerStudentName.getSelectedItem() != null ? spinnerStudentName.getSelectedItem().toString() : "");

        if (TextUtils.isEmpty(selectedYear) || TextUtils.isEmpty(selectedClass) || TextUtils.isEmpty(selectedTerm) || TextUtils.isEmpty(selectedStudentName)) {
            Toast.makeText(this, "Please make sure all selections are made", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> studentIds = (List<String>) spinnerStudentName.getTag();
        int selectedPosition = spinnerStudentName.getSelectedItemPosition();
        String selectedStudentId = studentIds != null ? studentIds.get(selectedPosition) : "";

        if (TextUtils.isEmpty(selectedStudentId)) {
            Toast.makeText(this, "Invalid student selection", Toast.LENGTH_SHORT).show();
            return;
        }

        checkForDuplicateAndSaveResults(selectedYear, selectedClass, selectedTerm, selectedStudentName, selectedStudentId);
    }

    private void checkForDuplicateAndSaveResults(String year, String className, String term, String studentName, String studentId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking for duplicates...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("results")
                .whereEqualTo("year", year)
                .whereEqualTo("className", className)
                .whereEqualTo("term", term)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(ManageResultsActivity.this, "Results for this term already exist", Toast.LENGTH_SHORT).show();
                    } else {
                        List<SubjectResult> results = new ArrayList<>();
                        for (int i = 0; i < tableLayoutSubjects.getChildCount(); i++) {
                            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
                            TextView subjectText = (TextView) row.getChildAt(0);

                            String subject = subjectText.getText().toString();
                            List<Double> scores = new ArrayList<>();
                            for (int j = 1; j <= 10; j++) {
                                View view = row.getChildAt(j);
                                if (view instanceof EditText) {
                                    EditText scoreInput = (EditText) view;
                                    String scoreStr = scoreInput.getText().toString();
                                    if (!TextUtils.isEmpty(scoreStr)) {
                                        double score = Double.parseDouble(scoreStr);
                                        if (score != 0.0) {
                                            scores.add(score);
                                        }
                                    }
                                }
                            }
                            if (!scores.isEmpty()) {
                                results.add(new SubjectResult(subject, scores));
                            }
                        }
                        saveToFirestore(year, className, term, studentName, studentId, results);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ManageResultsActivity.this, "Failed to check for duplicates", Toast.LENGTH_SHORT).show();
                });
    }

    private String sanitizeSpinnerValue(String value) {
        if (value == null) {
            return "";
        }

        try {
            value = value.trim();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return value;
    }

    private void saveToFirestore(String year, String className, String term, String studentName, String studentId, List<SubjectResult> results) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving results...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StudentResultData resultData = new StudentResultData(year, className, term, studentName, studentId, results);

        db.collection("results")
                .add(resultData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(ManageResultsActivity.this, "Results saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ManageResultsActivity.this, "Failed to save results", Toast.LENGTH_SHORT).show();
                });
    }

    public class StudentResultData {
        private String year;
        private String className;
        private String term;
        private String studentName;
        private String studentId;
        private List<SubjectResult> results;

        public StudentResultData() {}

        public StudentResultData(String year, String className, String term, String studentName, String studentId, List<SubjectResult> results) {
            this.year = year;
            this.className = className;
            this.term = term;
            this.studentName = studentName;
            this.studentId = studentId;
            this.results = results;
        }

        // Getters and setters for all fields
        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public List<SubjectResult> getResults() {
            return results;
        }

        public void setResults(List<SubjectResult> results) {
            this.results = results;
        }
    }
}
