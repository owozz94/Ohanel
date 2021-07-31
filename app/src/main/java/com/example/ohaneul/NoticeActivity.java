package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.ohaneul.adapter.NoticeAdapter;
import com.example.ohaneul.etc.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NoticeActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private static final String TAG = "NoticeActivity";
    private FirebaseFirestore firebaseFirestore;
    private NoticeAdapter noticeAdapter;
    private ArrayList<Post> postList;
    private boolean updating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);

        //RecyclerView Adapter 연결
        postList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(this, postList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noticeAdapter);

        setNotice(false);

    }

    private void setNotice(final boolean clear) {
        postList.clear();
        noticeAdapter.notifyDataSetChanged();
        updating = true;
        CollectionReference collectionReference = firebaseFirestore.collection("notice");
        collectionReference
                .orderBy("rate", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (clear) {
                                postList.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                postList.add(new Post(
                                        document.getData().get("title").toString(),
                                        document.getData().get("content").toString()
                                ));
                            }
                            noticeAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }//setNotice

}
