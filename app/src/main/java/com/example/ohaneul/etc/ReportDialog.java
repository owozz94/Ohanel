package com.example.ohaneul.etc;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.ohaneul.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportDialog {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;

    RadioGroup report;
    Button reportCheck, reportCancel;
    String reportItem;
    private Context context;

    public ReportDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final String docID, final String user) {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_report);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        report = dlg.findViewById(R.id.report);
        reportCheck = dlg.findViewById(R.id.report_chk);
        reportCancel = dlg.findViewById(R.id.report_cancel);

        RadioGroup.OnCheckedChangeListener reportChangeListener = new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.ad){
                    reportItem = "광고";
                }else if (checkedId == R.id.improper){
                    reportItem = "적절하지 않은 내용/사진";
                }else if (checkedId == R.id.lash){
                    reportItem = "욕설, 비난";
                }
            }
        };

        report.setOnCheckedChangeListener(reportChangeListener);

        reportCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reportItem != null) {

                    db = FirebaseFirestore.getInstance();

                    // Create a new user with a first, middle, and last name
                    Map<String, Object> map = new HashMap<>();
                    map.put("uid", user);
                    map.put("docID", docID);
                    map.put("category", reportItem);
                    map.put("date", new Timestamp(new Date()));

                    // Add a new document with a generated ID
                    db.collection("report").add(map);

                    Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    // 커스텀 다이얼로그를 종료한다.
                    dlg.dismiss();
                }else{
                    Toast.makeText(context, "신고 항목을 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reportCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });

    }//callFunction

}
