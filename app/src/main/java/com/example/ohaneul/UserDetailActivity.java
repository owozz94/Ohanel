package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    ImageView profileImage;
    EditText userName, introduction, passwordText, passwordAgain;
    TextView pwdcheck;
    RadioGroup gender;
    String genderCheck, localText, timeText, uid, email;
    Spinner localSpinner, timeSpinner;
    Button userUpdate, passwordUpdate;

    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseUser user;
    private String TAG;

    Uri filePath;
    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        //firebase 접근
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        }

        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.userName);
        introduction = findViewById(R.id.introduction);
        gender = findViewById(R.id.gender);
        gender.setOnCheckedChangeListener(genderChangeListener);
        localSpinner = findViewById(R.id.spinner_local);
        timeSpinner = findViewById(R.id.spinner_time);
        userUpdate = findViewById(R.id.userUpdate);
        passwordText = findViewById(R.id.password);
        passwordAgain = findViewById(R.id.passwordChk);
        pwdcheck = findViewById(R.id.password_check);
        passwordUpdate = findViewById(R.id.password_update);

        //비밀번호 확인
        passwordAgain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwd = passwordText.getText().toString();
                String pwdag = passwordAgain.getText().toString();

                if (pwd != null && pwdag != null){
                    if (pwd.equals(pwdag)){
                        pwdcheck.setText("비밀번호가 일치합니다.");
                        pwdcheck.setTextColor(Color.parseColor("#00E600"));
                    }else{
                        pwdcheck.setText("비밀번호가 다릅니다.");
                        pwdcheck.setTextColor(Color.parseColor("#FF4641"));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        setUI();

        //지역 카테고리
        String[] localStr = getResources().getStringArray(R.array.local);
        ArrayAdapter<String> localAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, localStr);
        localSpinner.setAdapter(localAdapter);
        localSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (localSpinner.getSelectedItemPosition() > 0) {
                    localText = localSpinner.getSelectedItem().toString();
                }//categoryText
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(UserDetailActivity.this, "지역을 선택해주세요.",Toast.LENGTH_SHORT).show();
            }
        });

        //시간 카테고리
        String[] timeStr = getResources().getStringArray(R.array.time);
        ArrayAdapter<String> timeAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, timeStr);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (timeSpinner.getSelectedItemPosition() > 0) {
                    timeText = timeSpinner.getSelectedItem().toString();
                }//categoryText
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(UserDetailActivity.this, "시간을 선택해주세요.",Toast.LENGTH_SHORT).show();
            }
        });

        //이미지를 선택
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });

        //프로필 수정버튼
        userUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
                if (userName != null && introduction != null && genderCheck != null && localText != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", userName.getText().toString());
                    map.put("introduction", introduction.getText().toString());
                    map.put("gender", genderCheck);
                    map.put("local", localText);
                    map.put("time", timeText);

                    db.collection("user").document(uid)
                            .update(map)
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

                    onBackPressed();
                }//if
            }
        });

        //비밀번호 수정
        passwordUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = passwordText.getText().toString();
                String pwdag = passwordAgain.getText().toString();

                if (pwd.equals(pwdag)){
                    user.updatePassword(passwordText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User password updated.");
                                    }
                                }
                            });
                    Toast.makeText(UserDetailActivity.this, "비밀번호 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }else {
                    Toast.makeText(UserDetailActivity.this, "비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }//if

            }//onClick
        });

    }//onCreate

    public void setUI(){
        DocumentReference docRef = db.collection("user").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        userName.setText(document.getData().get("name").toString());
                        introduction.setText(document.getData().get("introduction").toString());
                        if(document.getData().get("profileUrl") != null){
                            Glide.with(getApplicationContext())
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

    //성별 라디오 버튼
    RadioGroup.OnCheckedChangeListener genderChangeListener = new RadioGroup.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.women){
                genderCheck = "여자";
            }else if (checkedId == R.id.man){
                genderCheck = "남자";
            }else{
                genderCheck = "해당 없음";
            }
        }
    };//sexChangeListener

    //업로드할 파일이 있으면 수행
    private void uploadFile() {
        if (filePath != null) {
            //storage
            storage = FirebaseStorage.getInstance();
            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            Date now = new Date();
            filename = formatter.format(now);
            //storage 주소와 폴더 파일명을 지정해 준다.
            storageRef = storage.getReferenceFromUrl("gs://ohaneul-bc2ed.appspot.com/").child("user/thumbnail/" + filename);
            //올라가거라...
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
            Map<String, Object> map = new HashMap<>();
            map.put("deleteUrl", "user/thumbnail/" + filename);
            map.put("profileUrl", "https://firebasestorage.googleapis.com/v0/b/ohaneul-bc2ed.appspot.com/o/user%2Fthumbnail%2F"
                    +filename+"?alt=media");

            db.collection("user").document(uid)
                    .update(map)
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

        } else {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void imageDelete(String url) {
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
    }

}
