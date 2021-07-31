package com.example.ohaneul.adapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ohaneul.etc.Post;
import com.example.ohaneul.R;
import java.util.ArrayList;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.MainViewHolder> {

    TextView titleText;
    TextView contentText;

    private ArrayList<Post> mDataset;
    private Activity activity;
    private static final String TAG = "PostAdapter";

    // Item의 클릭 상태를 저장할 array 객체
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;

    static class MainViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;

        MainViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }
    }

    public NoticeAdapter(Activity activity, ArrayList<Post> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public NoticeAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        final NoticeAdapter.MainViewHolder mainViewHolder = new NoticeAdapter.MainViewHolder(linearLayout);
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
        titleText = linearLayout.findViewById(R.id.title_text);
        contentText = linearLayout.findViewById(R.id.content_text);

        //데이터 가져옴
        final Post post = mDataset.get(position);
        //데이터 넣음
        titleText.setText(" > "+post.getTitle());
        contentText.setText(post.getContent().replace("_n", "\n"));

        changeVisibility(selectedItems.get(position));
        //contentText.setVisibility(View.GONE);

        //클릭시
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItems.get(position)) {
                    // 펼쳐진 Item을 클릭 시
                    selectedItems.delete(position);
                } else {
                    // 직전의 클릭됐던 Item의 클릭상태를 지움
                    selectedItems.delete(prePosition);
                    // 클릭한 Item의 position을 저장
                    selectedItems.put(position, true);
                }
                // 해당 포지션의 변화를 알림
                if (prePosition != -1) notifyItemChanged(prePosition);
                notifyItemChanged(position);
                // 클릭된 position 저장
                prePosition = position;
            }
        });

    }

    /**
     * 클릭된 Item의 상태 변경
     * @param isExpanded Item을 펼칠 것인지 여부
     */
    private void changeVisibility(final boolean isExpanded) {
        // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열
        ValueAnimator va = isExpanded ? ValueAnimator.ofInt(0, 700) : ValueAnimator.ofInt(700, 0);
        // Animation이 실행되는 시간, n/1000초
        va.setDuration(500);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // imageView의 높이 변경
                contentText.getLayoutParams().height = (int) animation.getAnimatedValue();
                contentText.requestLayout();
                // imageView가 실제로 사라지게하는 부분
                contentText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            }
        });
        // Animation start
        va.start();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}