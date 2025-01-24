package com.example.luxv;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    private CalendarView calendarView;
    private TextView txtSelectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        txtSelectedDate = view.findViewById(R.id.txtSelectedDate);

        // Handle date selection
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            txtSelectedDate.setText("Selected Date: " + date);
        });

        return view;
    }
}
