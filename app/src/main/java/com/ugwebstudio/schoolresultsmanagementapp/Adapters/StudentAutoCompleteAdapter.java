package com.ugwebstudio.schoolresultsmanagementapp.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StudentAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private List<String> studentNames;
    private List<String> suggestions;

    public StudentAutoCompleteAdapter(@NonNull Context context, int resource, List<String> studentNames) {
        super(context, resource);
        this.studentNames = studentNames;
        this.suggestions = new ArrayList<>(studentNames);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int position) {
        return suggestions.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<String> filteredSuggestions = new ArrayList<>();
                if (constraint != null) {
                    for (String name : studentNames) {
                        if (name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredSuggestions.add(name);
                        }
                    }
                    filterResults.values = filteredSuggestions;
                    filterResults.count = filteredSuggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                suggestions.clear();
                if (results != null && results.count > 0) {
                    suggestions.addAll((List<String>) results.values);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}

