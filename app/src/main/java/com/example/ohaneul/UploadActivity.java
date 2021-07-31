package com.example.ohaneul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    ImageView image;
    EditText title, content;
    Button upload;
    private String TAG;
    private Uri filePath;
    private Spinner localSpinner, timeSpinner;
    private String localText, local, timeText;
    String folderdate, filename;

    String docID, uid;
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //firebase 접근
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        if(intent.getExtras().getString("detailDoc") == null){
            docID = null;
            uid = null;
        }else{
            docID = intent.getExtras().getString("detailDoc");
            uid = intent.getExtras().getString("detailUid");
        }

        image = findViewById(R.id.add_image);
        title = findViewById(R.id.titleText);
        content = findViewById(R.id.contentText);
        upload = findViewById(R.id.upload);
        localSpinner = findViewById(R.id.spinner_local);
        timeSpinner = findViewById(R.id.spinner_time);

        //지역 카테고리
        String[] str = getResources().getStringArray(R.array.local);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, str);
        localSpinner.setAdapter(adapter);
        localSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (localSpinner.getSelectedItemPosition() > 0) {
                    localText = localSpinner.getSelectedItem().toString();
                    local = "";
                    if (localSpinner.getSelectedItemPosition() == 1){
                        local = "seoul";
                    } else if (localSpinner.getSelectedItemPosition() == 2){
                        local = "gyeonggido";
                    } else if (localSpinner.getSelectedItemPosition() == 3){
                        local = "gangwondo";
                    } else if (localSpinner.getSelectedItemPosition() == 4){
                        local = "chungcheongdo";
                    } else if (localSpinner.getSelectedItemPosition() == 5){
                        local = "jeollado";
                    } else {
                        local = "gyeongsangdo";
                    }//local
                }//categoryText
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(UploadActivity.this, "지역을 선택해주세요.",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UploadActivity.this, "시간대를 선택해주세요.",Toast.LENGTH_SHORT).show();
            }
        });

        //업로드일때
        if (docID == null && uid == null){
            //이미지를 선택
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
                }
            });
            //내용&사진 업로드
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadFile();
                    String titleText = title.getText().toString();
                    String contentText = content.getText().toString();
                    if (titleText != null && contentText != null && localSpinner.getSelectedItemPosition() > 0) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("uid",user.getUid());
                        map.put("title", titleText);
                        map.put("content", contentText);
                        map.put("date", new Timestamp(new Date()));
                        map.put("local", localText);
                        map.put("time", timeText);
                        map.put("deleteUrl", "local/" + local + "/" + folderdate + "/" + filename);
                        map.put("url", "https://firebasestorage.googleapis.com/v0/b/ohaneul-bc2ed.appspot.com/o/local%2F"
                                +local+"%2F"+folderdate+"%2F"+filename+"?alt=media");
                        map.put("bookmarkCount", 0);
                        map.put("emoticonCount1", 0);
                        map.put("emoticonCount2", 0);
                        map.put("emoticonCount3", 0);
                        map.put("emoticonCount4", 0);

                        // Add a new document with a generated ID
                        db.collection("post").add(map);

                        onBackPressed();
                    }else {
                        Toast.makeText(UploadActivity.this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }//if (titleText != null && contentText != null)
                }
            });

        }
        //수정일때
        else{
            localSpinner.setVisibility(View.GONE);
            timeSpinner.setVisibility(View.GONE);
            setUI(docID);
            //내용&사진 업로드
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String titleText = title.getText().toString();
                    String contentText = content.getText().toString();

                    if (titleText != null && contentText != null ) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("title", titleText);
                        map.put("content", contentText);

                        db.collection("post").document(docID)
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
                    }else {
                        Toast.makeText(UploadActivity.this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }//if (titleText != null && contentText != null)
                }
            });

        }

    }

    //업로드할 파일이 있으면 수행
    private void uploadFile() {
        if (filePath != null) {
            //storage
            storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            SimpleDateFormat formatter_folderdate = new SimpleDateFormat("yyyy");
            Date now = new Date();
            filename = formatter.format(now);
            folderdate = formatter_folderdate.format(now);
            //storage 주소와 폴더 파일명을 지정해 준다.
            storageRef = storage.getReferenceFromUrl("gs://ohaneul-bc2ed.appspot.com/").child("local/" + local + "/" + folderdate + "/" + filename);
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
        } else {
            Toast.makeText(getApplicationContext(), "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
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
                image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                        title.setText(document.getData().get("title").toString());
                        content.setText(document.getData().get("content").toString());
                        if(document.getData().get("url") != null){
                            Glide.with(getApplicationContext())
                                    .load(document.getData().get("url"))
                                    .into(image);
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
