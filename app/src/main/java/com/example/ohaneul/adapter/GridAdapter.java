package com.example.ohaneul.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ohaneul.DetailActivity;
import com.example.ohaneul.etc.Post;
import com.example.ohaneul.R;

import java.util.ArrayList;



public class GridAdapter extends RecyclerView.Adapter<GridAdapter.MainViewHolder>{

    ImageView imageContent;

    private ArrayList<Post> mDataset;
    private Activity activity;
    private static final String TAG = "GridAdapter";

    static class MainViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        MainViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }
    public GridAdapter(Activity activity, ArrayList<Post> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @NonNull
    @Override
    //실행에 드는 비용을 줄일려고 ViewHolder를 사용
    //ViewHolder 생성 시에 . ViewType에 따라 여러 타입의 ViewHolder를 생성할 수도 있습니다.
    //처음 화면에 보이는 View에 대해 ViewHolder가 생성되며, 이후 스크롤 시에 보여지는 View들은 재활용되는 View이므로 다시 호출되지 않습니다.
    public GridAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_thumbnail, parent, false);

        final GridAdapter.MainViewHolder mainViewHolder = new GridAdapter.MainViewHolder(linearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return mainViewHolder;
    }

    //반복할 레이아웃 연결
    //화면에 ViewHolder가 붙을 때마다 호출됩니다. 화면에 새로운 View가 붙을 때마다 호출 되므로, 실질적인 데이터 처리는 이곳에서 이루어집니다.
    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        LinearLayout linearLayout = holder.linearLayout;
        imageContent = linearLayout.findViewById(R.id.image_content);
        final Post post = mDataset.get(position);

        //이미지 데이터
        if (mDataset.get(position).getImage() != null) {
            Glide.with(activity).load(mDataset.get(position).getImage()).into(imageContent);
        }

        //클릭시 Detail로 이동
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("document",post.getDocID());
                intent.putExtra("uid",post.getUid());
                activity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}