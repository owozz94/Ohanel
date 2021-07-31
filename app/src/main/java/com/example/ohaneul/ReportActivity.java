package com.example.ohaneul;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ohaneul.adapter.Report_Adapter;
import com.example.ohaneul.etc.Report_post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReportActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private static final String TAG = "ReportActivity";
    private FirebaseFirestore firebaseFirestore;
    private Report_Adapter report_postAdapter;
    private ArrayList<Report_post> postList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        recyclerView=findViewById(R.id.recyclerView_report);
        //RecyclerView Adapter 연결
        postList = new ArrayList<>();
        report_postAdapter = new Report_Adapter(this, postList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(report_postAdapter);

        setReport();

    }
    private void setReport() {
        postList.clear();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("report")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                postList.add(new Report_post(

                                        document.getData().get("category").toString(),
                                        document.getDate("date"),
                                        document.getData().get("email").toString(),
                                        document.getData().get("docID").toString(),
                                        document.getData().get("uid").toString()

                                ));
                            }
                            report_postAdapter.notifyDataSetChanged();
                        }
                         else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}