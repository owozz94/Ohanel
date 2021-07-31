package com.example.ohaneul.etc;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    // 여러 액티비티 한번에 종료하기
    public static ArrayList<Activity> actList = new ArrayList<Activity>();

    public void actFinish(){
        for(int i = 0; i < actList.size(); i++)
            actList.get(i).finish();
    }

}
