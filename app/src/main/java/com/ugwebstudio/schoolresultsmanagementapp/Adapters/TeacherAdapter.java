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
import com.ugwebstudio.schoolresultsmanagementapp.classes.Teacher;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {
    private List<Teacher> teachers;
    private TeacherAdapter.OnItemClickListener listener;
    private TeacherAdapter.OnItemLongClickListener longClickListener;

    public TeacherAdapter(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public interface OnItemClickListener {
        void onItemClick(Teacher teacher);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(Teacher teacher);
    }

    public void setOnItemLongClickListener(TeacherAdapter.OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
    public void setOnItemClickListener(TeacherAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Teacher teacher = teachers.get(position);
        holder.bind(teacher,listener,longClickListener);
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView subjectTextView;
        private TextView phoneTextView;
        private TextView emailTextView;
        private ImageView editTeacherImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            subjectTextView = itemView.findViewById(R.id.txt_teacher_subject);
            phoneTextView = itemView.findViewById(R.id.txt_teacher_phone);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
            editTeacherImageView = itemView.findViewById(R.id.image_edit);

        }

        public void bind(Teacher teacher,OnItemClickListener listener,OnItemLongClickListener longClickListener) {
            nameTextView.setText(teacher.getName());
            subjectTextView.setText(teacher.getSubject());
            phoneTextView.setText(teacher.getPhone());
            emailTextView.setText(teacher.getEmail());

            //edit teacher
            editTeacherImageView.setOnClickListener(view -> {
                if (listener != null){
                    listener.onItemClick(teacher);
                }

            });

            //long press to delete
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null){
                        longClickListener.onItemLongClick(teacher);
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
