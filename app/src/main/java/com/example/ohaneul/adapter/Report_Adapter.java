package com.example.ohaneul.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ohaneul.DetailActivity;
import com.example.ohaneul.R;
import com.example.ohaneul.etc.Report_post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Report_Adapter extends RecyclerView.Adapter<Report_Adapter.MainViewHolder> {

    TextView category;
    TextView report_date;
    TextView report_email;
    Button post_chk,report_chk;

    private ArrayList<Report_post> mDataset;
    private Activity activity;
    private static final String TAG = "Report_PostAdapter";


    static class MainViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;

        MainViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }
    public Report_Adapter(Activity activity, ArrayList<Report_post> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(linearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return mainViewHolder;
    }

    //반복할 레이아웃 연결
    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, final int position) {
        LinearLayout linearLayout = holder.linearLayout;
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득

        category = linearLayout.findViewById(R.id.category);
        report_date = linearLayout.findViewById(R.id.report_date);
        report_email = linearLayout.findViewById(R.id.report_email);
        post_chk = linearLayout.findViewById(R.id.post_chk); //복사하려는 줄 뒤에 ctrl+d
        report_chk = linearLayout.findViewById(R.id.report_chk);

        SimpleDateFormat sfd = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        //데이터 가져옴
        final Report_post report_post = mDataset.get(position);
        //데이터 넣음
        category.setText("카테고리 : " + report_post.getCategory());
        report_date.setText("신고시간 : " + sfd.format(report_post.getDate()));
        report_email.setText("신고자 이메일 :  " + report_post.getReport_email());


        post_chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("document",report_post.getdocID()); //name은 건드리면 안됨.
                intent.putExtra("uid",report_post.getUid());
                activity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}