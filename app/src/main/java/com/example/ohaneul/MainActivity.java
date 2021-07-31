package com.example.ohaneul;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.ohaneul.etc.BackPressedForFinish;
import com.example.ohaneul.etc.BaseActivity;
import com.example.ohaneul.fragment.FavorActivity;
import com.example.ohaneul.fragment.HomeActivity;
import com.example.ohaneul.fragment.RecentlyActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends BaseActivity {

    FirebaseUser user;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeActivity homeActivity = new HomeActivity();
    private FavorActivity favorActivity = new FavorActivity();
    private RecentlyActivity recentlyActivity = new RecentlyActivity();
    BackPressedForFinish backPressedForFinish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actList.add(this);

        user = FirebaseAuth.getInstance().getCurrentUser();

        backPressedForFinish = new BackPressedForFinish(this);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, homeActivity).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());

        //Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(!task.isSuccessful()){
                            Log.w("FCM Log","getInstanceId failed",task.getException());
                            return;
                        }
                        String  token = task.getResult().getToken();
                        Log.d("FCM Log","FCM 토큰"+token);
                      //  Toast.makeText(MainActivity.this,token,Toast.LENGTH_SHORT).show();
                    }
                });
    }

    class ItemSelectedListener
            implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch(menuItem.getItemId())
            {
                case R.id.tab1:
                    transaction.replace(R.id.frameLayout, homeActivity)
                            .commitAllowingStateLoss();
                    break;
                case R.id.tab2:
                    transaction.replace(R.id.frameLayout, favorActivity)
                            .commitAllowingStateLoss();
                    break;
                case R.id.tab3:
                    transaction.replace(R.id.frameLayout, recentlyActivity)
                            .commitAllowingStateLoss();
                    break;
                case R.id.tab4:
                    //transaction.replace(R.id.frameLayout, mypageActivity).commitAllowingStateLoss();
                    Intent intent = new Intent(MainActivity.this, MypageActivity.class);
                    intent.putExtra("docUid", user.getUid());
                    startActivity(intent);
                    break;
            }
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        backPressedForFinish.onBackPressed();
    }
}
