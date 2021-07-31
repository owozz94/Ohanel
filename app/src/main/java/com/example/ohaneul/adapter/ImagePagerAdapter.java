package com.example.ohaneul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.ohaneul.R;

import java.util.ArrayList;

public class ImagePagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<Integer> imageList;

    public ImagePagerAdapter(Context context, ArrayList<Integer> imageList)
    {
        this.mContext = context;
        this.imageList = imageList;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_slide, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        /*
        if(imageList.get(position).getImage() != null){
            Glide.with(mContext).load(imageList.get(position).getImage()).into(imageView);
        }
         */
        imageView.setImageResource(imageList.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
