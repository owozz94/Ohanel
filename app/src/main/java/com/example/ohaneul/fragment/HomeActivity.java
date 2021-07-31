package com.example.ohaneul.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.ohaneul.etc.Post;
import com.example.ohaneul.R;
import com.example.ohaneul.UploadActivity;
import com.example.ohaneul.adapter.HorizontalSlideImgAdapter;
import com.example.ohaneul.adapter.ImagePagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends Fragment {

    ViewGroup viewGroup;
    FloatingActionButton fab_main;
    String docID, uid;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView localTitle, timeTitle;
    RecyclerView localRecyclerView, timeRecyclerView;

    private static final String TAG = "HomeActivity";
    private FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    private HorizontalSlideImgAdapter localImgAdapter, timeImgAdapter;
    private ArrayList<Post> localList, timeList;
    private boolean updating;

    ArrayList<Integer> imageList;
    private static final int DP = 24;
    int currentPage = 0;
    Timer timer;
    //delay in milliseconds before task is to be executed
    final long DELAY_MS = 500;
    // time in milliseconds between successive task executions.
    final long PERIOD_MS = 3000;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_home, container, false);
        final Context context = container.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        viewPager = viewGroup.findViewById(R.id.viewPager);
        tabLayout = viewGroup.findViewById(R.id.tabs);
        fab_main = viewGroup.findViewById(R.id.fab_main);
        localTitle = viewGroup.findViewById(R.id.local_title);
        localRecyclerView = viewGroup.findViewById(R.id.horizontalImageSlide_local);
        timeTitle = viewGroup.findViewById(R.id.time_title);
        timeRecyclerView = viewGroup.findViewById(R.id.horizontalImageSlide_time);


        //광고 슬라이드쇼
        this.InitializeData();
        viewPager.setClipToPadding(false);
        tabLayout.setupWithViewPager(viewPager, true);
        viewPager.setAdapter(new ImagePagerAdapter(context, imageList));
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if(currentPage == imageList.size()) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);


        //업로드 버튼
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.putExtra("detailDoc", docID);
                intent.putExtra("detailUid", uid);
                startActivity(intent);
            }
        });


        //가로 이미지 스크롤-지역
        localList = new ArrayList<>();
        localImgAdapter = new HorizontalSlideImgAdapter(getActivity(), localList);
        localRecyclerView.setHasFixedSize(true);
        localRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        localRecyclerView.setAdapter(localImgAdapter);
        horizontalImgUpdate(false, "local", localList, localImgAdapter);

        //가로 이미지 스크롤-시간
        timeList = new ArrayList<>();
        timeImgAdapter = new HorizontalSlideImgAdapter(getActivity(), timeList);
        timeRecyclerView.setHasFixedSize(true);
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        timeRecyclerView.setAdapter(timeImgAdapter);
        horizontalImgUpdate(false, "time", timeList, timeImgAdapter);

        return viewGroup;
    }


    private void InitializeData() {
        imageList = new ArrayList();

        imageList.add(R.drawable.milky_way);
        imageList.add(R.drawable.sunset);
        imageList.add(R.drawable.pier);
    }

    private void horizontalImgUpdate(final boolean clear,
                                     final String field,final ArrayList<Post> list, final HorizontalSlideImgAdapter adapter) {
        updating = true;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("user").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        if(field == "local"){
                            localTitle.setText("▷ '"+document.getData().get(field).toString()+"' 지역의 하늘은 어떤가요?");
                        }else if(field == "time"){
                            timeTitle.setText("▷ '"+document.getData().get(field).toString()+"' 시간의 하늘은 어떤가요?");
                        }

                        CollectionReference collectionReference = firebaseFirestore.collection("post");
                        collectionReference .whereEqualTo(field, document.getData().get(field).toString())
                                .orderBy("date", Query.Direction.DESCENDING).limit(5)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if(clear){
                                                list.clear();
                                            }
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                list.add(new Post(
                                                        document.getData().get("url") == null ? null : document.getData().get("url").toString(),
                                                        document.getData().get("title").toString(),
                                                        document.getId(),
                                                        document.getData().get("uid").toString()
                                                ));
                                            }
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                        updating = false;
                                    }
                                });
                    } //if (document.exists())
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

}

