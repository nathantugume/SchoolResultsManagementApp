package com.ugwebstudio.schoolresultsmanagementapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.admin.StudentResultsActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ViewReportActivity;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentResults;

import java.util.ArrayList;
import java.util.List;

public class StudentReportsAdapter extends RecyclerView.Adapter<StudentReportsAdapter.ViewHolder> {
    private List<StudentResults> studentResultsList;
    private List<StudentResults> filteredList;

    public StudentReportsAdapter(List<StudentResults> studentResultsList) {
        this.studentResultsList = new ArrayList<>(studentResultsList);
        this.filteredList = new ArrayList<>(studentResultsList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentResults studentResults = filteredList.get(position);
        holder.bind(studentResults);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateList(List<StudentResults> newList) {
        studentResultsList.clear();
        studentResultsList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged(); // Notify adapter of data change
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView classTextView;

        private TextView termTextView;

        private CardView resultsCard;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textView_name);
            classTextView = itemView.findViewById(R.id.textView_class);
            termTextView = itemView.findViewById(R.id.textView_term);
            resultsCard = itemView.findViewById(R.id.results_card);

        }

        public void bind(StudentResults studentResults) {
            nameTextView.setText(studentResults.getStudent());
            classTextView.setText(studentResults.getStudentClass());
            termTextView.setText(studentResults.getTerm());
            resultsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Open a new activity with the student results for the selected term and class
                    Intent intent = new Intent(view.getContext(), ViewReportActivity.class);
                    intent.putExtra("selectedClass", studentResults.getStudentClass());
                    intent.putExtra("selectedTerm", studentResults.getTerm());
                    intent.putExtra("studentId", studentResults.getStudentId());
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    public void filterList(String searchText) {
        filteredList.clear();
        if (searchText.isEmpty()) {
            filteredList.addAll(studentResultsList); // Add all items if search query is empty
        } else {
            String filterPattern = searchText.toLowerCase().trim();
            for (StudentResults studentResults : studentResultsList) {
                if (studentResults.getStudent().toLowerCase().contains(filterPattern)) {
                    filteredList.add(studentResults);
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }
}
