package com.ugwebstudio.schoolresultsmanagementapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.Student;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentClass;

import java.util.List;

public class StudentClassAdapter extends RecyclerView.Adapter<StudentClassAdapter.ViewHolder> {
    private List<StudentClass> studentClassList; // Rename the member variable

    public StudentClassAdapter(List<StudentClass> studentClassList) { // Correct the constructor
        this.studentClassList = studentClassList; // Assign the parameter to the member variable
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_class_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentClass studentClass = studentClassList.get(position);
        holder.bind(studentClass);
    }

    @Override
    public int getItemCount() {
        return studentClassList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView levelTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            levelTextView = itemView.findViewById(R.id.textViewLevel);
        }

        public void bind(StudentClass studentClass) {
            nameTextView.setText(studentClass.getClassName());
            levelTextView.setText(studentClass.getLevel());
        }
    }
}
