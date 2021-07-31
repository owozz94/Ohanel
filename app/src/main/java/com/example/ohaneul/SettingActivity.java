package com.example.ohaneul;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.ohaneul.etc.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends BaseActivity {

    private static final String TAG = "SettingActivity";
    TextView logoutView, leaveView, userChange, notice, userPrivacy;

    String uid, email;
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        actList.add(this);

        logoutView = findViewById(R.id.logout_view);
        leaveView = findViewById(R.id.leave_view);
        userChange = findViewById(R.id.userchange_view);
        notice = findViewById(R.id.notice_view);
        userPrivacy = findViewById(R.id.user_privacy);

        //firebase 접근
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        }

        userChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, UserDetailActivity.class);
                startActivity(intent);
            }
        });

        notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, NoticeActivity.class);
                startActivity(intent);
            }
        });

        userPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, PrivacyActivity.class);
                startActivity(intent);
            }
        });

        leaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("회원탈퇴").setMessage("정말 탈퇴하시겠습니까?")
                        .setPositiveButton("탈퇴하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("user").document(user.getUid())
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
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(SettingActivity.this,"계정이 삭제되었습니다. 다음에 또 봐요 ㅠㅠ"
                                                ,Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        FirebaseAuth.getInstance().signOut();
                                        startActivity(intent);
                                        actFinish();
                                    }
                                });

                            }
                        })//setPositiveButton
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                Toast.makeText(SettingActivity.this,"사랑합니다♡",Toast.LENGTH_SHORT).show();;
                            }
                        });builder.show();
            }//onClick
        });//logoutButton.setOnClickListener

        //로그아웃
        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(SettingActivity.this, LoginActivity.class);
                                i.putExtra("state", "kill");
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                FirebaseAuth.getInstance().signOut();
                                startActivity(i);
                                actFinish();
                            }
                        })//setPositiveButton
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();//setNegativeButton
            }//onClick
        });//logoutButton.setOnClickListener

    }
}
