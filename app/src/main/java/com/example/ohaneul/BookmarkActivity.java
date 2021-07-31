package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.ohaneul.adapter.FavorAdapter;
import com.example.ohaneul.etc.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    static FirebaseAuth auth;
    FirebaseUser user;
    String email, uid, docUid;
    boolean postChk = false;
    TextView test;

    private static final String TAG = "BookmarkActivity";
    FirebaseFirestore db;
    private FavorAdapter favorAdapter;
    private ArrayList<Post> postList;
    private boolean updating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        }

        Intent intent = getIntent();
        docUid = intent.getExtras().getString("docUid");

        //RecyclerView Adapter 연결
        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        favorAdapter = new FavorAdapter(this, postList);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(favorAdapter);
        setPost(false);

    }

    public void setPost(final boolean clear) {
        updating = true;
        CollectionReference collectionReference = db.collection("post");
        collectionReference
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(clear){
                                postList.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                postChk(document.getId());
                            }
                            favorAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }

    public void postChk(final String docID){
        db.collection("post").document(docID).collection("bookmark")
                .whereEqualTo("uid", docUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                postChk=true;
                                if (postChk == true) {
                                    DocumentReference docRef = db.collection("post").document(docID);
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                    int rate = 0;
                                                    postList.add(new Post(
                                                            rate,
                                                            document.getData().get("url") == null ? null : document.getData().get("url").toString(),
                                                            document.getData().get("title").toString(),
                                                            document.getData().get("local").toString(),
                                                            document.getData().get("time").toString(),
                                                            document.getData().get("bookmarkCount").toString(),
                                                            document.getId(),
                                                            document.getData().get("uid").toString()
                                                    ));
                                                    favorAdapter.notifyDataSetChanged();
                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });
                                }//if (postChk == true)
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }//postChk

}
