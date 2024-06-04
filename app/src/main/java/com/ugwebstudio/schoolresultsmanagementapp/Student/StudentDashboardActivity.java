package com.ugwebstudio.schoolresultsmanagementapp.Student;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.SelectUserActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.StudentReportActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ViewReportActivity;
import com.ugwebstudio.schoolresultsmanagementapp.classes.Student;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentClass;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentResults;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDashboardActivity extends AppCompatActivity {
    private Spinner spinnerClass, spinnerTerms;
    private FirebaseFirestore db;
    private TableLayout tableLayout;
    private String studentId;
    private ProgressDialog progressDialog;
    private String selectedClass;
    private String selectedTerm;
    private TextView studentNameTxt;
    private List<Integer> aggregatesList = new ArrayList<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView no_results_text_view;
    private LinearLayout reportContent;
    private ImageView studentProfileImageView;


    private static final Map<String, Integer> gradePoints = new HashMap<>();
    private float average;
    private MaterialToolbar toolbar;
    private ShimmerFrameLayout shimmerFrameLayout;

    static {
        gradePoints.put("A", 1);
        gradePoints.put("B", 2);
        gradePoints.put("C", 3);
        gradePoints.put("D", 4);
        gradePoints.put("P", 8);
        gradePoints.put("F", 9);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        ExtendedFloatingActionButton btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(view -> printReport());
        reportContent = findViewById(R.id.printContent);

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        no_results_text_view = findViewById(R.id.txt_no_results);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.sign_out) {
                mAuth.signOut();
                startActivity(new Intent(StudentDashboardActivity.this, SelectUserActivity.class));
                return true;
            }
            return false;
        });
        studentId = mAuth.getUid();

        spinnerClass = findViewById(R.id.spinner_class);
        spinnerTerms = findViewById(R.id.spinner_term);
        db = FirebaseFirestore.getInstance();
        tableLayout = findViewById(R.id.table_layout);
        studentNameTxt = findViewById(R.id.student_name);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Results...");
        progressDialog.setCancelable(false);

        progressDialog.show();

        fetchStudentDetails( studentId);
        // Listeners for class and term selection
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClass = parent.getItemAtPosition(position).toString();
                loadTerms();
                Log.d("StudentDashboard", "Class selected: " + selectedClass);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no class is selected
            }
        });

        spinnerTerms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTerm = parent.getItemAtPosition(position).toString();
                fetchStudentResults(selectedClass, selectedTerm, studentId);
                progressDialog.show();
                shimmerFrameLayout.startShimmer();
                Log.d("StudentDashboard", "Term selected: " + selectedTerm);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no term is selected
            }
        });

        // Load classes from Firestore
        loadClassesFromFirestore();
    }


    private void loadTerms() {
        Log.d("StudentDashboard", "Loading terms...");
        String[] termsArray = {"Term One", "Term Two", "Term Three"};
        ArrayAdapter<String> termsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, termsArray);
        termsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTerms.setAdapter(termsAdapter);
        progressDialog.dismiss();

    }

    private void fetchStudentResults(String selectedClass, String selectedTerm, String studentId) {
        Log.d("StudentDashboard", "Fetching results for Class: " + selectedClass + ", Term: " + selectedTerm + ", Student ID: " + studentId);
        if ("Term Three".equalsIgnoreCase(selectedTerm)) {
            fetchAndCalculateThirdTerm(selectedClass, studentId);
        } else {
            fetchAndDisplayRegularResults(selectedClass, selectedTerm, studentId);
        }
    }

    private void fetchAndDisplayRegularResults(String selectedClass, String selectedTerm, String studentId) {
        db.collection("results")
                .whereEqualTo("class", selectedClass)
                .whereEqualTo("term", selectedTerm)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        no_results_text_view.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        progressDialog.dismiss();
                        Map<String, Map<String, Integer>> subjectResults = new HashMap<>();
                        task.getResult().forEach(document -> {
                            StudentResults studentResults = document.toObject(StudentResults.class);
                            studentNameTxt.setText(studentResults.getStudent());

                            String subject = document.getString("subject");
                            String resultType = document.getString("resultType");
                            int marks = studentResults.getMarks();

                            Log.d("StudentDashboard", "Fetched result - Subject: " + subject + ", Result Type: " + resultType + ", Marks: " + marks);

                            subjectResults.putIfAbsent(subject, new HashMap<>());
                            subjectResults.get(subject).put(resultType, marks);
                        });

                        subjectResults.forEach((subject, resultTypes) -> addTableRow(subject, resultTypes));
                    } else {
                        Snackbar.make(StudentDashboardActivity.this.getCurrentFocus(), "Results not fetched!!", Snackbar.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        shimmerFrameLayout.stopShimmer();
                        Log.e("StudentDashboard", "Error getting results: ", task.getException());
                    }
                });
    }

    private void fetchAndCalculateThirdTerm(String selectedClass, String studentId) {
        db.collection("results")
                .whereEqualTo("class", selectedClass)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        no_results_text_view.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        progressDialog.dismiss();
                        Map<String, List<Integer>> subjectMarks = new HashMap<>();
                        task.getResult().forEach(document -> {
                            String subject = document.getString("subject");
                            int marks = document.getLong("marks").intValue();

                            Log.d("StudentDashboard", "Fetched third term result - Subject: " + subject + ", Marks: " + marks);

                            subjectMarks.computeIfAbsent(subject, k -> new ArrayList<>()).add(marks);
                        });

                        subjectMarks.forEach((subject, marksList) -> {
                            double average = marksList.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                            calculateGradeFromAverage(average, new ViewReportActivity.GradeCallback() {
                                @Override
                                public void onGradeComputed(String grade, int aggregate, String division) {
                                    addTableRow(subject, Collections.singletonMap("Third Term", (int) average), grade);
                                }
                            });
                        });
                    } else {
                        Snackbar.make(StudentDashboardActivity.this.getCurrentFocus(), "Error fetching third term results!!", Snackbar.LENGTH_LONG).show();
                        shimmerFrameLayout.stopShimmer();
                        progressDialog.dismiss();
                        Log.e("StudentDashboard", "Error getting third term results: ", task.getException());
                    }
                });
    }

    private void calculateGradeFromAverage(double average, ViewReportActivity.GradeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("gradingScales")
                .orderBy("from")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String grade = "Not Graded";
                        int aggregate = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if ("o_level".equalsIgnoreCase(document.getString("level"))) {
                                float minMark = document.getLong("from");
                                float maxMark = document.getLong("to");
                                if (average >= minMark && average <= maxMark) {
                                    grade = document.getString("grade");
                                    aggregate = calculateAggregate(grade);
                                    break;
                                }
                            }
                        }

                        String division = getDivision(aggregate);
                        Log.d("StudentDashboard", "Calculated grade: " + grade + ", Aggregate: " + aggregate + ", Division: " + division);
                        callback.onGradeComputed(grade, aggregate, division);
                    } else {
                        Log.w("StudentDashboard", "Error getting grading scales.", task.getException());
                    }
                });
    }

    private String getDivision(int totalAggregate) {
        if (totalAggregate <= 32) {
            return "Division I";
        } else if (totalAggregate <= 45) {
            return "Division II";
        } else if (totalAggregate <= 58) {
            return "Division III";
        } else if (totalAggregate <= 72) {
            return "Division IV";
        } else {
            return "Ungraded";
        }
    }

    private int calculateAggregate(String grade) {
        return gradePoints.getOrDefault(grade, 9);
    }

    private void addTableRow(String subject, Map<String, Integer> resultTypes) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        setRowBackgroundColor(row, tableLayout.getChildCount());

        TextView subjectTextView = createTextView(subject);
        row.addView(subjectTextView);

        EditText beginningEditText = createEditText(getMarksString(resultTypes, "Beginning of Term"));
        EditText midtermEditText = createEditText(getMarksString(resultTypes, "Midterm"));
        EditText endEditText = createEditText(getMarksString(resultTypes, "End of Term"));

        calculateGrades(getMarks(resultTypes, "Beginning of Term"), getMarks(resultTypes, "Midterm"), getMarks(resultTypes, "End of Term"), new ViewReportActivity.GradeCallback() {
            @Override
            public void onGradeComputed(String grade, int aggregate, String division) {
                TextView gradeTextView = createTextView(grade);
                TextView aggregatesTxt = findViewById(R.id.aggregates);
                TextView divisionTxt = findViewById(R.id.report_division);

                Collections.sort(aggregatesList);
                Collections.reverse(aggregatesList);
                int totalAggregate = 0;
                for (int i = 0; i < Math.min(8, aggregatesList.size()); i++) {
                    totalAggregate += aggregatesList.get(i);
                }
                String div = getDivision(totalAggregate);
                aggregatesTxt.setText(String.valueOf(totalAggregate));
                divisionTxt.setText(div);

                row.addView(gradeTextView);
            }
        });

        row.addView(beginningEditText);
        row.addView(midtermEditText);
        row.addView(endEditText);

        tableLayout.addView(row);
    }

    private void addTableRow(String subject, Map<String, Integer> resultTypes, String grade) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        setRowBackgroundColor(row, tableLayout.getChildCount());

        TextView subjectTextView = createTextView(subject);
        row.addView(subjectTextView);

        EditText marksEditText = createEditText(getMarksString(resultTypes, "Third Term"));

        TextView gradeTextView = createTextView(grade);
        row.addView(gradeTextView);
        row.addView(marksEditText);

        tableLayout.addView(row);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private EditText createEditText(String text) {
        EditText editText = new EditText(this);
        editText.setText(text);
        editText.setPadding(8, 8, 8, 8);
        return editText;
    }

    private String getMarksString(Map<String, Integer> resultTypes, String key) {
        return resultTypes.containsKey(key) ? String.valueOf(resultTypes.get(key)) : "";
    }

    private int getMarks(Map<String, Integer> resultTypes, String key) {
        return resultTypes.getOrDefault(key, 0);
    }

    private void setRowBackgroundColor(TableRow row, int index) {
        int backgroundColor = index % 2 == 0 ? ContextCompat.getColor(this, R.color.colorAccent) : ContextCompat.getColor(this, R.color.white);
        row.setBackgroundColor(backgroundColor);
    }

    private void calculateGrades(int beginningMarks, int midMarks, int endMarks, ViewReportActivity.GradeCallback callback) {
        float average = ((float) beginningMarks + (float) midMarks + (float) endMarks) / 3;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("gradingScales")
                .orderBy("from")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String[] grades = {"Not Graded"};
                        int aggregate = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if ("o_level".equalsIgnoreCase(document.getString("level"))) {
                                float minMark = document.getLong("from");
                                float maxMark = document.getLong("to");
                                if (average >= minMark && average <= maxMark) {
                                    grades[0] = document.getString("grade");
                                    aggregate = calculateAggregate(grades[0]); // Calculate aggregate for this grade
                                    aggregatesList.add(aggregate);

                                    break;
                                }
                            }
                        }

                        String division = getDivision(aggregate); // Determine the division based on the aggregate
                        Log.d("StudentDashboard", "Calculated grade: " + grades[0] + ", Aggregate: " + aggregate + ", Division: " + division);
                        callback.onGradeComputed(grades[0], aggregate, division);
                    } else {
                        Log.w("StudentDashboard", "Error getting grading scales.", task.getException());
                    }
                });
    }

    // Method to load classes
    private void loadClassesFromFirestore() {
        db.collection("classes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> classNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                StudentClass studentClass = document.toObject(StudentClass.class);
                                classNames.add(studentClass.getClassName());
                                Log.d("StudentDashboard", "Loaded class: " + studentClass.getClassName());
                            }
                            ArrayAdapter<String> classAdapter = new ArrayAdapter<>(StudentDashboardActivity.this, android.R.layout.simple_spinner_item, classNames);
                            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerClass.setAdapter(classAdapter);
                        } else {
                            Toast.makeText(StudentDashboardActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
                            Log.e("StudentDashboard", "Error loading classes: ", task.getException());
                        }
                    }
                });
    }

    private void printReport() {
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = new ViewPrintAdapter(this, reportContent, "Student Report");
        printManager.print("Student Report", printAdapter, new PrintAttributes.Builder().build());
    }

    private class ViewPrintAdapter extends PrintDocumentAdapter {
        private Context context;
        private View view;
        private String jobName;

        public ViewPrintAdapter(Context context, View view, String jobName) {
            this.context = context;
            this.view = view;
            this.jobName = jobName;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                pdfDocument.close();
                pdfDocument = null;
                return;
            }

            view.draw(page.getCanvas());
            pdfDocument.finishPage(page);

            try (FileOutputStream output = new FileOutputStream(destination.getFileDescriptor())) {
                pdfDocument.writeTo(output);
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                pdfDocument.close();
            }

            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        }
    }

    private void fetchStudentDetails(String studentId) {
        DocumentReference studentRef = db.collection("students").document(studentId);
        studentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Student student = document.toObject(Student.class);
                    if (student != null) {
                        displayStudentDetails(student);
                    } else {
                        Toast.makeText(this, "Failed to fetch student details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No such student", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Fetch failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStudentDetails(Student student) {
        studentNameTxt.setText(student.getName());
        // Populate other UI elements with student details

        ImageView imageView = findViewById(R.id.student_profile);

        // Load the student image using Glide
        if (student.getImageUrl() != null && !student.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(student.getImageUrl())
                    .placeholder(R.drawable.ic_student)
                    .into(imageView);


        }
    }
}
