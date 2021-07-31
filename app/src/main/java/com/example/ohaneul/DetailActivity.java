package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.ohaneul.etc.ReportDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener{

    String docID, uid;
    TextView profileName, titleText, localText, timeText, dateText, contentText;
    TextView icon1count, icon2count, icon3count, icon4count;
    ImageView icon1, icon2, icon3, icon4;
    ImageView profileImage, popUpMenu, imageContent, bookmark;
    boolean bookmarkChk, icon1Chk, icon2Chk, icon3Chk, icon4Chk;
    Button report;
    String mUid, mEmail;

    FirebaseStorage storage;
    FirebaseFirestore db;
    FirebaseUser user;
    private static final String TAG = "DetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mEmail = user.getEmail();
            mUid = user.getUid();
        }

        Intent intent = getIntent();
        docID = intent.getExtras().getString("document");
        uid = intent.getExtras().getString("uid");

        profileName = findViewById(R.id.profile_name);
        profileImage = findViewById(R.id.profile_image);
        popUpMenu = findViewById(R.id.popup_menu);
        titleText = findViewById(R.id.title_text);
        localText = findViewById(R.id.local_text);
        timeText = findViewById(R.id.time_text);
        dateText = findViewById(R.id.date_text);
        contentText = findViewById(R.id.content_text);
        bookmark = findViewById(R.id.bookmark_image);
        imageContent = findViewById(R.id.image_content);
        icon1 = findViewById(R.id.icon_1);
        icon2 = findViewById(R.id.icon_2);
        icon3 = findViewById(R.id.icon_3);
        icon4 = findViewById(R.id.icon_4);
        icon1count = findViewById(R.id.icon_1_count);
        icon2count = findViewById(R.id.icon_2_count);
        icon3count = findViewById(R.id.icon_3_count);
        icon4count = findViewById(R.id.icon_4_count);
        report = findViewById(R.id.report);

        //즐겨찾기 기능
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmarkChk == false) {
                    bookmark.setImageResource(R.drawable.ic_bookmark);
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid",user.getUid());
                    db.collection("post").document(docID).collection("bookmark").document(user.getUid()).set(map);
                    fieldCount("bookmarkCount", docID, 1);
                    bookmarkChk = true;
                } else {
                    db.collection("post").document(docID).collection("bookmark").document(uid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                    fieldCount("bookmarkCount", docID, -1);
                    bookmark.setImageResource(R.drawable.ic_bookmark_border);
                    bookmarkChk = false;
                }
            }
        });

        //팝업메뉴
        popUpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);//v는 클릭된 뷰를 의미
                getMenuInflater().inflate(R.menu.menu_detail, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.post_update:
                                Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
                                intent.putExtra("detailDoc", docID);
                                intent.putExtra("detailUid", uid);
                                startActivity(intent);
                                break;
                            case R.id.post_delete:
                                DocumentReference docRef = db.collection("post").document(docID);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                    postDelete(docID, document.getData().get("deleteUrl").toString());
                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });//postDelete(docID);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popup.show();//Popup Menu 보이기
            }
        });

        //이모티콘
        icon1.setOnClickListener(this);
        icon2.setOnClickListener(this);
        icon3.setOnClickListener(this);
        icon4.setOnClickListener(this);

        //작성자 페이지로 이동
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid == user.getUid()) {
                    Intent intent = new Intent(getApplicationContext(), MypageActivity.class);
                    intent.putExtra("docUid", user.getUid());
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(), MypageActivity.class);
                    intent.putExtra("docUid", uid);
                    startActivity(intent);
                }
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportDialog reportDialog = new ReportDialog(DetailActivity.this);
                reportDialog.callFunction(docID, user.getUid());
            }
        });

        setUI(docID);

        getUser(uid);

    }

    public void setUI(String docID){
        DocumentReference docRef = db.collection("post").document(docID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        SimpleDateFormat sfd = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

                        titleText.setText("Title: "+document.getData().get("title").toString());
                        localText.setText("#"+document.getData().get("local").toString());
                        timeText.setText("#"+document.getData().get("time").toString());
                        dateText.setText(sfd.format(document.getDate("date")));
                        contentText.setText(document.getData().get("content").toString());
                        if(document.getData().get("url") != null){
                            Glide.with(getApplicationContext())
                                    .load(document.getData().get("url"))
                                    .into(imageContent);
                        }
                        icon1count.setText(document.getData().get("emoticonCount1").toString());
                        icon2count.setText(document.getData().get("emoticonCount2").toString());
                        icon3count.setText(document.getData().get("emoticonCount3").toString());
                        icon4count.setText(document.getData().get("emoticonCount4").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        db.collection("post").document(docID).collection("bookmark").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        bookmark.setImageResource(R.drawable.ic_bookmark);
                        bookmarkChk=true;
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        db.collection("post").document(docID).collection("emoticon").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if(document.getData().get("icon1") != null){
                            icon1.setImageResource(R.drawable.sun);
                            icon1Chk=true;
                        }else if(document.getData().get("icon2") != null){
                            icon2.setImageResource(R.drawable.rain);
                            icon2Chk=true;
                        }else if(document.getData().get("icon3") != null){
                            icon3.setImageResource(R.drawable.happy);
                            icon3Chk=true;
                        }else if(document.getData().get("icon4") != null){
                            icon4.setImageResource(R.drawable.cloud);
                            icon4Chk=true;
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

    public void getUser(String uid){
        db.collection("user").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if(document.getData().get("profileUrl") != null){
                            Glide.with(getApplicationContext())
                                    .load(document.getData().get("profileUrl"))
                                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(100)))
                                    .into(profileImage);
                        }
                        profileName.setText(document.getData().get("name").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void postDelete(String docID, String url) {
        db.collection("post").document(docID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference(url);

        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

        onBackPressed();

    }

    //이모티콘
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon_1:
                if (icon1Chk == false && icon2Chk == false && icon3Chk == false && icon4Chk == false){
                    icon1.setImageResource(R.drawable.sun);
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid",user.getUid());
                    map.put("icon1","icon1");
                    db.collection("post").document(docID).collection("emoticon").document(user.getUid()).set(map);
                    fieldCount("emoticonCount1", docID, 1);
                    changeCount(icon1count, "emoticonCount1", 1);
                    icon1Chk = true;
                }else if (icon1Chk == true){
                    db.collection("post").document(docID).collection("emoticon").document(uid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!"); }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                            });
                    icon1.setImageResource(R.drawable.sunmono);
                    fieldCount("emoticonCount1", docID, -1);
                    changeCount(icon1count, "emoticonCount1", -1);
                    icon1Chk = false;
                    }
                break;
            case R.id.icon_2:
                if (icon1Chk == false && icon2Chk == false && icon3Chk == false && icon4Chk == false){
                    icon2.setImageResource(R.drawable.rain);
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid",user.getUid());
                    map.put("icon2","icon2");
                    db.collection("post").document(docID).collection("emoticon").document(user.getUid()).set(map);
                    fieldCount("emoticonCount2", docID, 1);
                    changeCount(icon2count, "emoticonCount2", 1);
                    icon2Chk = true;
                }else if (icon2Chk == true){
                    db.collection("post").document(docID).collection("emoticon").document(uid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                    icon2.setImageResource(R.drawable.rainmono);
                    fieldCount("emoticonCount2", docID, -1);
                    changeCount(icon2count, "emoticonCount2", -1);
                    icon2Chk = false;
                }
                break;
            case R.id.icon_3:
                if (icon1Chk == false && icon2Chk == false && icon3Chk == false && icon4Chk == false){
                    icon3.setImageResource(R.drawable.happy);
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid",user.getUid());
                    map.put("icon3","icon3");
                    db.collection("post").document(docID).collection("emoticon").document(user.getUid()).set(map);
                    fieldCount("emoticonCount3", docID, 1);
                    changeCount(icon3count, "emoticonCount3", 1);
                    icon3Chk = true;
                }else if (icon3Chk == true){
                    db.collection("post").document(docID).collection("emoticon").document(uid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                    icon3.setImageResource(R.drawable.happymono);
                    fieldCount("emoticonCount3", docID, -1);
                    changeCount(icon3count, "emoticonCount3", -1);
                    icon3Chk = false;
                }
                break;
            case R.id.icon_4:
                if (icon1Chk == false && icon2Chk == false && icon3Chk == false && icon4Chk == false){
                    icon4.setImageResource(R.drawable.cloud);
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid",user.getUid());
                    map.put("icon4","icon4");
                    db.collection("post").document(docID).collection("emoticon").document(user.getUid()).set(map);
                    fieldCount("emoticonCount4", docID, 1);
                    changeCount(icon4count, "emoticonCount4", 1);
                    icon4Chk = true;
                }else if (icon4Chk == true){
                    db.collection("post").document(docID).collection("emoticon").document(uid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                    icon4.setImageResource(R.drawable.cloudmono);
                    fieldCount("emoticonCount4", docID, -1);
                    changeCount(icon4count, "emoticonCount4", -1);
                    icon4Chk = false;
                }
                break;
        }//switch
    }

    public void changeCount(final TextView view, final String field, final int count){
        db.collection("post").document(docID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        view.setText(Integer.toString(Integer.parseInt(document.getData().get(field).toString()) + count));

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void fieldCount(final String field, final String docID, final int count){
        db.collection("post").document(docID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if (count > 0){
                            db.collection("post").document(docID)
                                    .update(field, Integer.parseInt(document.getData().get(field).toString()) + count)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        }else if (Integer.parseInt(document.getData().get(field).toString())+count >= 0) {
                            db.collection("post").document(docID)
                                    .update(field, Integer.parseInt(document.getData().get(field).toString()) + count)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
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
