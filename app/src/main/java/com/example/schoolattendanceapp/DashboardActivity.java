package com.example.schoolattendanceapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolattendanceapp.adapters.ClassAdapter;
import com.example.schoolattendanceapp.models.ClassItems;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class DashboardActivity extends AppCompatActivity {
    //DECLARE COMPONENTS
    FloatingActionButton fab;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ClassAdapter classAdapter;
    ArrayList<ClassItems> classItems = new ArrayList<>();
    Toolbar toolbar;
    DbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DbHelper(this);
    //FETCH ALL COMPONENT IDS
        fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(v -> showDialog());
        loadData();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter = new ClassAdapter(this, classItems);
        recyclerView.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));

        setToolbar();

    }

    //LOAD EXISTING CLASS DETAILS
    private void loadData() {
        Cursor cursor = dbHelper.getClassTable();
        classItems.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.C_ID));
            String className = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.CLASS_NAME_KEY));
            String division = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.DIVISION_NAME_KEY));

            classItems.add(new ClassItems(id,className,division));
        }
    }
    //SET TOOLBAR
    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText("School Attendance");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
    }

    //PASSING CLASS DATA INTO STUDENT ACTIVITY
    private void gotoItemActivity(int position) {
        Intent intent = new Intent(this, StudentActivity.class);

        intent.putExtra("className", classItems.get(position).getClassName());
        intent.putExtra("division", classItems.get(position).getDivision());
        intent.putExtra("position", position);
        intent.putExtra("cid", classItems.get(position).getCid());

        startActivity(intent);
    }
    //SHOW CONTEXT DIALOG TO ADD CLASS
    private void showDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.CLASS_ADD_DIALOG);
        dialog.setListener((className, division) -> addClass(className, division));
    }

    private void addClass(String className, String division) {
        long cid = dbHelper.addClass(className,division);
        ClassItems classItem = new ClassItems(cid,className, division);
        classItems.add(classItem);

        classAdapter.notifyDataSetChanged();

    }
    //SELECTED ITEM MENU FOR CLASS
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                showUpdateDialog(item.getGroupId());
                break;
            case 1:
                deleteClass(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }
    //UPDATE CLASS DETAILS
    private void showUpdateDialog(int position) {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className,division)->updateClass(position,className,division));
    }

    private void updateClass(int position, String className, String division) {
        dbHelper.updateClass(classItems.get(position).getCid(),className,division);
        classItems.get(position).setClassName(className);
        classItems.get(position).setDivision(division);
        classAdapter.notifyItemChanged(position);

    }
    //FOR DELETE CLASS
    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }
}