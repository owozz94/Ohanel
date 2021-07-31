package com.example.ohaneul;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.ohaneul.adapter.GridAdapter;
import com.example.ohaneul.etc.BaseActivity;
import com.example.ohaneul.etc.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MypageActivity extends BaseActivity {

    static FirebaseAuth auth;
    FirebaseUser user;
    TextView userNameLocal, userIntrodiction;
    ImageView profileImage, bookmark, setting_btn;
    String docUid, email, uid;
    private String TAG;
    private FirebaseFirestore firebaseFirestore;
    private GridAdapter gridAdapter;
    private ArrayList<Post> postList;
    private boolean updating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        actList.add(this);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        }

        Intent intent = getIntent();
        docUid = intent.getExtras().getString("docUid");

        userNameLocal = findViewById(R.id.user_name_local);
        userIntrodiction = findViewById(R.id.user_introdiction);
        profileImage = findViewById(R.id.profile_image);
        bookmark = findViewById(R.id.bookmark);
        setting_btn = findViewById(R.id.setting_btn);

        //RecyclerView Adapter 연결
        firebaseFirestore = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        gridAdapter = new GridAdapter(this,postList);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        //격자무늬
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setAdapter(gridAdapter);

        postsUpdate(false);

        getData();

        //북마크한 게시글만
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, BookmarkActivity.class);
                intent.putExtra("docUid", docUid);
                startActivity(intent);
            }
        });

        //셋팅액티비티 이동
        setting_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MypageActivity.this, SettingActivity.class);
                startActivity(i);
            }
        });

    }

    private void postsUpdate(final boolean clear) {
        updating = true;
        CollectionReference collectionReference = firebaseFirestore.collection("post");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //데이터 입력
        collectionReference .whereEqualTo("uid", docUid)
                .orderBy("date", Query.Direction.DESCENDING)
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
                                        document.getData().get("url") ==null?null : document.getData().get("url").toString(),
                                        document.getId(),
                                        document.getData().get("uid").toString()
                                ));
                            }
                            gridAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }

    //유저 name 갖고오기
    private void getData(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(docUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                        userNameLocal.setText("Name: "+document.get("name").toString()+" / Like: "+document.get("local").toString()+", "+document.get("time").toString());
                                        userIntrodiction.setText(document.get("introduction").toString());
                                        if(document.getData().get("profileUrl") != null){
                                            Glide.with(MypageActivity.this)
                                                    .load(document.getData().get("profileUrl"))
                                                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(100)))
                                                    .into(profileImage);
                                        }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

}