package com.example.ohaneul.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.ohaneul.DetailActivity;
import com.example.ohaneul.etc.Post;
import com.example.ohaneul.R;

import java.util.ArrayList;

public class HorizontalSlideImgAdapter  extends RecyclerView.Adapter<HorizontalSlideImgAdapter.MainViewHolder> {

    ImageView thumbnail;
    TextView titleText;

    private ArrayList<Post> mDataset;
    private Activity activity;
    private static final String TAG = "HorizontalSlideImgAdapter";

    static class MainViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        MainViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }

    public HorizontalSlideImgAdapter(Activity activity, ArrayList<Post> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @NonNull
    @Override
    public HorizontalSlideImgAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horizontalimg, parent, false);
        final HorizontalSlideImgAdapter.MainViewHolder mainViewHolder = new HorizontalSlideImgAdapter.MainViewHolder(linearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        LinearLayout linearLayout = holder.linearLayout;


        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        titleText = linearLayout.findViewById(R.id.title_text);
        thumbnail = linearLayout.findViewById(R.id.thumbnail);

        final Post post = mDataset.get(position);
        titleText.setText("title: "+post.getTitle());
        if(mDataset.get(position).getThumbnail() != null){
            Glide.with(activity)
                    .load(mDataset.get(position).getThumbnail())
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(20)))
                    .into(thumbnail);
        }

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
