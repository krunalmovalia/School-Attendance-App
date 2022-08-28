package com.example.schoolattendanceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.schoolattendanceapp.adapters.StudentAdapter;
import com.example.schoolattendanceapp.models.StudentItem;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {

    Toolbar toolbar;
    private String className, division;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        division = intent.getStringExtra("division");
        position = intent.getIntExtra("className",-1);

        setToolbar();
        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        studentAdapter = new StudentAdapter(this, studentItems);
        recyclerView.setAdapter(studentAdapter);
        studentAdapter.setOnItemClickListener(position->changeStatus(position));


    }

    private void changeStatus(int position) {
        String status = studentItems.get(position).getStatus();

        if (status.equals("P")) status = "A";
        else status = "P";

        studentItems.get(position).setStatus(status);
        studentAdapter.notifyItemChanged(position);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText(className);
        subtitle.setText(division);
        back.setOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem->onMenuItemSelected(menuItem));

    }

    private boolean onMenuItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId()==R.id.add_student){
            showAddStudentDialog();
        }
        return true;
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll,name)->addStudent(roll,name));
    }

    private void addStudent(String roll, String name) {
        studentItems.add(new StudentItem(roll,name));
        studentAdapter.notifyDataSetChanged();
    }

}