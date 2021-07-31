package com.example.ohaneul.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ohaneul.etc.Post;
import com.example.ohaneul.R;
import com.example.ohaneul.adapter.PostAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecentlyActivity extends Fragment {

    ViewGroup viewGroup;

    Spinner localSpinner, timeSpinner;
    String localText, timeText = null;

    RecyclerView recyclerView;

    private static final String TAG = "RecentlyActivity";
    private FirebaseFirestore firebaseFirestore;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private boolean updating;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_recently, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView = viewGroup.findViewById(R.id.recyclerView);
        localSpinner = viewGroup.findViewById(R.id.spinner_local);
        timeSpinner = viewGroup.findViewById(R.id.spinner_time);

        //지역 카테고리
        String[] str = getResources().getStringArray(R.array.local);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, str);
        localSpinner.setAdapter(adapter);
        localSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(timeText != "time" && localSpinner.getSelectedItemPosition() > 0){
                    localText = localSpinner.getSelectedItem().toString();
                    doubleFilter(false, localText, timeText);
                }else if (timeText == "time" && localSpinner.getSelectedItemPosition() > 0) {
                    localText = localSpinner.getSelectedItem().toString();
                    postsFilter(false, "local", localText);
                }else if (timeText != "time" && localSpinner.getSelectedItemPosition() == 0) {
                    postsFilter(false, "time", timeText);
                    localText = "local";
                }else if (timeText == "time" && localSpinner.getSelectedItemPosition() == 0) {
                    postsUpdate(false);
                    localText = "local";
                }
            }//onItemSelected
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "지역을 선택해주세요.",Toast.LENGTH_SHORT).show();
            }
        });

        //시간 카테고리
        String[] timeStr = getResources().getStringArray(R.array.time);
        ArrayAdapter<String> timeAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, timeStr);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(localText != "local" && timeSpinner.getSelectedItemPosition() > 0){
                    timeText = timeSpinner.getSelectedItem().toString();
                    doubleFilter(false, localText, timeText);
                }else if (localText == "local" && timeSpinner.getSelectedItemPosition() > 0) {
                    timeText = timeSpinner.getSelectedItem().toString();
                    postsFilter(false, "time", timeText);
                }else if (localText != "local" && timeSpinner.getSelectedItemPosition() == 0) {
                    postsFilter(false, "local", localText);
                    timeText = "time";
                }else if (localText == "local" && timeSpinner.getSelectedItemPosition() == 0) {
                    postsUpdate(false);
                    timeText = "time";
                }
            }//onItemSelected
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "시간대를 선택해주세요.",Toast.LENGTH_SHORT).show();
            }
        });

        //RecyclerView Adapter 연결
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getActivity(), postList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(postAdapter);

        return viewGroup;
    }

    //데이터 입력
    public void postsUpdate(final boolean clear) {
        postList.clear();
        postAdapter.notifyDataSetChanged();
        updating = true;
        CollectionReference collectionReference = firebaseFirestore.collection("post");
        collectionReference
                .orderBy("date", Query.Direction.DESCENDING).limit(20)
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

                                postList.add(new Post(
                                        document.getData().get("title").toString(),
                                        "#"+document.getData().get("local").toString(),
                                        "#"+document.getData().get("time").toString(),
                                        document.getDate("date"),
                                        document.getData().get("content").toString(),
                                        document.getData().get("url") == null ? null : document.getData().get("url").toString(),
                                        document.getId(),
                                        document.getData().get("uid").toString()
                            ));
                        }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }

    //데이터 필터
    public void postsFilter(final boolean clear, String filter, String field) {
        postList.clear();
        postAdapter.notifyDataSetChanged();
        updating = true;
        CollectionReference collectionReference = firebaseFirestore.collection("post");
        collectionReference
                .whereEqualTo(filter, field).orderBy("date", Query.Direction.DESCENDING).limit(20)
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
                                postList.add(new Post(
                                        document.getData().get("title").toString(),
                                        "#"+document.getData().get("local").toString(),
                                        "#"+document.getData().get("time").toString(),
                                        document.getDate("date"),
                                        document.getData().get("content").toString(),
                                        document.getData().get("url") == null ? null : document.getData().get("url").toString(),
                                        document.getId(),
                                        document.getData().get("uid").toString()
                                ));
                            }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }

    public void doubleFilter(final boolean clear, String local, String time){
        postList.clear();
        postAdapter.notifyDataSetChanged();
        updating = true;
        CollectionReference collectionReference = firebaseFirestore.collection("post");
        collectionReference.whereEqualTo("local", local).whereEqualTo("time", time)
                .orderBy("date", Query.Direction.DESCENDING).limit(20)
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
                                postList.add(new Post(
                                        document.getData().get("title").toString(),
                                        "#"+document.getData().get("local").toString(),
                                        "#"+document.getData().get("time").toString(),
                                        document.getDate("date"),
                                        document.getData().get("content").toString(),
                                        document.getData().get("url") == null ? null : document.getData().get("url").toString(),
                                        document.getId(),
                                        document.getData().get("uid").toString()
                                ));
                            }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }

}

