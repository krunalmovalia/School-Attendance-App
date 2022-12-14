package com.example.schoolattendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.schoolattendanceapp.adapters.StudentAdapter;
import com.example.schoolattendanceapp.models.ClassItems;
import com.example.schoolattendanceapp.models.StudentItem;

import java.util.ArrayList;
import java.util.Objects;

public class StudentActivity extends AppCompatActivity {

    //DECLARE THE COMPONENTS
    Toolbar toolbar;
    private String className, division;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();
    DbHelper dbHelper;
    private int cid;
    private MyCalendar calendar;
    private TextView subtitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        calendar = new MyCalendar();
        dbHelper = new DbHelper(this);

        // MOVE VALUE TO NEXT ACTIVITY
        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        division = intent.getStringExtra("division");
        position = intent.getIntExtra("className",-1);
        cid = intent.getIntExtra("cid",-1);


        setToolbar();
        loadData();
        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        studentAdapter = new StudentAdapter(this, studentItems);
        recyclerView.setAdapter(studentAdapter);
        studentAdapter.setOnItemClickListener(position->changeStatus(position));
        loadStatusData();


    }

    //TO LOAD THE EXISTING STUDENT DATA
    private void loadData() {
        Cursor cursor = dbHelper.getStudentTable(cid);
        studentItems.clear();
        while (cursor.moveToNext()){
            long sid = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.S_ID));
            int roll = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.STUDENT_ROLL_KEY));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.STUDENT_NAME_KEY));
            studentItems.add(new StudentItem(sid,roll,name));
        }
        cursor.close();
    }

    //TO MARK ABSENT AND PRESENT
    private void changeStatus(int position) {
        String status = studentItems.get(position).getStatus();

        if (status.equals("P")) status = "A";
        else status = "P";

        studentItems.get(position).setStatus(status);
        studentAdapter.notifyItemChanged(position);
    }
    //SET THE TOOLBAR
    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);
        save.setOnClickListener(v -> saveStatus());

        title.setText(className);
        subtitle.setText(division+" | "+calendar.getDate());
        back.setOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this,R.drawable.ic_more));
        toolbar.setOnMenuItemClickListener(menuItem->onMenuItemSelected(menuItem));

    }

    //TO STORE DATA OF PRESENT AND ABSENT IN DATABASE
    private void saveStatus() {
        for (StudentItem studentItem : studentItems){
            String  status = studentItem.getStatus();
            if (!Objects.equals(status, "P")) status = "A";
            long value = dbHelper.addStatus(studentItem.getSid(),cid,calendar.getDate(),status);
            if (value==-1)dbHelper.updateStatus(studentItem.getSid(),calendar.getDate(),status);
        }
    }

    //TO SHOW STORED DATA
    private void loadStatusData(){
        for (StudentItem studentItem : studentItems){
            String  status =dbHelper.getStatus(studentItem.getSid(),calendar.getDate());
            if (status!=null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        studentAdapter.notifyDataSetChanged();
    }

    //MORE MENU
    private boolean onMenuItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId()==R.id.add_student){
            showAddStudentDialog();
            }
        else if (menuItem.getItemId()==R.id.show_calendar){
                showCalendar();
            }
        else if (menuItem.getItemId()==R.id.show_attendance_sheet){
            openSheetList();
        }
        return true;
    }

    //THROUGH INTENT DATA PASSING IN SHEET LIST
    private void openSheetList() {
        long[] idArray = new long[studentItems.size()];
        String[] nameArray = new String[studentItems.size()];
        int[] rollArray = new int[studentItems.size()];

        for (int i=0 ; i<idArray.length; i++)
            idArray[i] = studentItems.get(i).getSid();

        for (int i=0 ; i<rollArray.length; i++)
            rollArray[i] = studentItems.get(i).getRoll();

        for (int i=0 ; i<nameArray.length; i++)
            nameArray[i] = studentItems.get(i).getName();


        Intent intent = new Intent(this,SheetListActivity.class);
        intent.putExtra("cid",cid);
        intent.putExtra("idArray",idArray);
        intent.putExtra("rollArray",rollArray);
        intent.putExtra("nameArray",nameArray);
        startActivity(intent);
    }
    //SHOW THE CALENDAR
    private void showCalendar() {

        calendar.show(getSupportFragmentManager(),"");
        calendar.setOnCalendarOkClickListener(this::onCalendarOkClicked);
    }

    //TO SET CALENDER AND DIV DATA IN TOOLBAR
    private void onCalendarOkClicked(int year, int month, int day) {
        calendar.setDate(year, month, day);
        subtitle.setText(division+" | "+calendar.getDate());
        loadStatusData();
    }

    //TO SHOW STUDENT ADD DIALOG
    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll,name)->addStudent(roll,name));
    }

    //TO ADD STUDENT
    private void addStudent(String roll_string, String name) {
        int roll =  Integer.parseInt(roll_string);
        long sid = dbHelper.addStudent(cid,roll,name);
        StudentItem studentItem = new StudentItem(sid,roll,name);
        studentItems.add(studentItem);
        studentAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    //UPDATE STUDENT DETAILS DIALOG
    private void showUpdateStudentDialog(int position) {
        MyDialog dialog = new MyDialog(studentItems.get(position).getRoll(),studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string,name)->updateStudent(position,name));

    }

    //TO UPDATE STUDENT
    private void updateStudent(int position, String name) {

        dbHelper.updateStudent(studentItems.get(position).getSid(),name);
        studentItems.get(position).setName(name);
        studentAdapter.notifyItemChanged(position);

    }
    //TO DELETE STUDENT
    private void deleteStudent(int position) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        studentAdapter.notifyItemChanged(position);
    }

}