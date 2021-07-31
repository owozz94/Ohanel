package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ohaneul.etc.BackPressedForFinish;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    //이메일 로그인 모듈 변수
    private FirebaseAuth auth;
    //현재 로그인된 유저 정보
    private FirebaseUser currentUser;
    FirebaseFirestore db;

    EditText emailText, passwordText;
    Button login, signUp;
    ProgressBar progress_bar;
    private String TAG;

    BackPressedForFinish backPressedForFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase의 회원정보를 가져오기 위해 연결
        auth =  FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        login = findViewById(R.id.loginButton);
        signUp = findViewById(R.id.signupButton);
        progress_bar = findViewById(R.id.progress_bar);

        backPressedForFinish = new BackPressedForFinish(this);

        //회원가입 페이지로 이동
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });

        //로그인 버튼
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLogin();
            }
        });

    }

    //이메일 로그인
    private void emailLogin() {
        final String email = emailText.getText().toString().trim();
        String pwd = passwordText.getText().toString().trim();
        if (email.isEmpty() || pwd.isEmpty()){
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.",Toast.LENGTH_SHORT).show();
        }else{
            progress_bar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email,pwd)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                DocumentReference docRef = db.collection("user").document(auth.getUid());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
                            }else{
                                progress_bar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this,"로그인 오류", Toast.LENGTH_SHORT).show();
                            }//else
                        }//onComplete
                    });//signInWithEmailAndPassword
        }//else
    }//emailLogin

    @Override
    public void onBackPressed() {
        backPressedForFinish.onBackPressed();
    }
}
