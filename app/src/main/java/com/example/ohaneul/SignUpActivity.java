package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText emailText, passwordText, passwordAgain, nameText;
    TextView pwdcheck;
    RadioGroup gender;
    String genderCheck;
    Button signUp, emailAuth;
    Spinner localSpinner, timeSpinner;
    String localText, timeText;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;

    ProgressDialog mDialog;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //firebase 접근
        firebaseAuth =  FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);
        passwordAgain = findViewById(R.id.passwordChk);
        pwdcheck = findViewById(R.id.password_check);
        nameText = findViewById(R.id.name);
        signUp = findViewById(R.id.signUp);
        gender = findViewById(R.id.gender);
        gender.setOnCheckedChangeListener(genderChangeListener);
        localSpinner = findViewById(R.id.spinner_local);
        timeSpinner = findViewById(R.id.spinner_time);

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
                Toast.makeText(SignUpActivity.this, "지역을 선택해주세요.",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SignUpActivity.this, "시간을 선택해주세요.",Toast.LENGTH_SHORT).show();
            }
        });

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

        //가입하기 버튼
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailText != null && passwordText != null && genderCheck != null && nameText != null && localText != null && timeText != null){
                    signUp();
                }else{
                    Toast.makeText(SignUpActivity.this, "빈칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }//onCreate

    //뒤로가기
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void signUp(){
        //가입정보 가져오기
        final String email = emailText.getText().toString().trim();
        String pwd = passwordText.getText().toString().trim();
        final String userName = nameText.getText().toString();

        if(pwd.equals(passwordAgain.getText().toString())) {
            final ProgressDialog mDialog = new ProgressDialog(SignUpActivity.this);
            mDialog.setMessage("가입중입니다...");
            mDialog.show();

            //신규 계정 등록
            firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //가입 성공시
                    if (task.isSuccessful()) {
                        mDialog.dismiss();

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        String name = userName;
                        String gender = genderCheck;

                        // Create a new user with a first, middle, and last name
                        Map<String, Object> map = new HashMap<>();
                        map.put("email", email);
                        map.put("name", name);
                        map.put("gender", gender);
                        map.put("profileUrl",
                                "https://firebasestorage.googleapis.com/v0/b/ohaneul-bc2ed.appspot.com/o/user%2Fdefault%2Fuser.png?alt=media&token=692a742a-be7e-427a-843f-7ecd3358d850");
                        map.put("local", localText);
                        map.put("time", timeText);
                        map.put("introduction", "...");

                        // Add a new document with a generated ID
                        db.collection("user").document(user.getUid()).set(map);

                        //가입이 이루어졌을시 가입 화면을 빠져나감.
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(SignUpActivity.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "이미 존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
                        return;  //해당 메소드 진행을 멈추고 빠져나감.
                    }
                }//onComplete
            });//createUserWithEmailAndPassword
        }else{
            Toast.makeText(SignUpActivity.this, "비밀번호가 틀렸습니다. 다시 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }//userPassword.equals(passwordAgain.getText().toString())
    }

    public int EtNullCheck(EditText a){
        return a.getText().toString().length();
    }

    public int SpNullCheck(Spinner b){
        return b.getSelectedItem().toString().length();
    }

}
