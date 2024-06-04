package com.ugwebstudio.schoolresultsmanagementapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<Student> students;
    private List<Student> studentList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private List<Student> filteredList;


    public void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(students);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Student student : students) {
                if (student.getName().toLowerCase().contains(filterPattern)) {
                    filteredList.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void setInitialList(List<Student> initialList) {
        studentList.clear();
        studentList.addAll(initialList);
        filteredList.clear();
        filteredList.addAll(initialList);
        notifyDataSetChanged();
    }






    public interface OnItemClickListener {
        void onItemClick(Student student);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(Student student);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public StudentAdapter(List<Student> students) {

        this.students = students;
        this.studentList = new ArrayList<>(students);
        this.filteredList = new ArrayList<>(students); // Initialize the filtered list with all students

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = filteredList.get(position);
        holder.bind(student, listener, longClickListener);

        // Example: Accessing currentStudent based on the position in students list
     //   Student currentStudent = students.get(position);
    }


    @Override
    public int getItemCount() {

        return filteredList.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView classTextView;
        private TextView txt_std_id;

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            classTextView = itemView.findViewById(R.id.textViewClass);
            txt_std_id = itemView.findViewById(R.id.txt_std_id);
            imageView = itemView.findViewById(R.id.image_edit);
        }

        public void bind(Student student, OnItemClickListener listener,OnItemLongClickListener longClickListener) {
            nameTextView.setText(student.getName());
            classTextView.setText(student.getStudentClass());
            txt_std_id.setText(student.getId());

            //edit button
            imageView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onItemClick(student);
                }

            });

            //long press to delete
            itemView.setOnLongClickListener(view -> {
                if (longClickListener != null){
                    longClickListener.onItemLongClick(student);
                    return true;
                }
                return false;
            });
        }
    }
}
