package com.ericcode.pagersnaphelperdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        // VerticalPagerSnapHelper与RecyclerView建立连接
        new VerticalPagerSnapHelper().attachToRecyclerView(recyclerView);
        // 让smoothToPosition()时，滚动到该item的顶部
        TopPositionLayoutManager layout = new TopPositionLayoutManager(this);
        recyclerView.setLayoutManager(layout);
        recyclerView.setAdapter(new MyAdapter());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    int listenPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    int listenPosition = layoutManager.findLastVisibleItemPosition();
                    Log.i("tag", "onScrollStateChanged listenPosition:" + listenPosition);
                    if (listenPosition == -1) {
                        return;
                    }
                    View viewByPosition = layoutManager.findViewByPosition(listenPosition);
                    int measuredHeight = recyclerView.getMeasuredHeight();
                    // 滑动到下一条，但是只滑动了一点，最终不会滑动到下一条
                    if (viewByPosition != null && viewByPosition.getTop() > measuredHeight / 2) {
                        Log.i("tag", "dirty data");
                    }

                    Log.i("tag", "on position change:" + listenPosition);
                }
            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        List<String> list;

        private RecyclerView recyclerView;

        public MyAdapter() {
            this.list = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                int total = (new Random().nextInt(10) + 30);
//                total = 19;
                for (int j = 0; j < total; j++) {
                    stringBuilder
                            .append("position:")
                            .append(i)
                            .append(" ")
                            .append("第")
                            .append(j)
                            .append("行")
                            .append(" ")
                            .append("共:")
                            .append(total)
                            .append("行");

                    if (j < total - 1) {
                        stringBuilder.append("\n");
                    } else {
                        stringBuilder.append("\n");
                        stringBuilder.append("\n");
                        stringBuilder.append("\n");
                        stringBuilder.append("\n");
                        stringBuilder.append("\n");
                    }

                }
                list.add(stringBuilder.toString());
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
            viewHolder.tv.setText(list.get(i));
            viewHolder.pageNumber.setText("第" + i + "页");
            viewHolder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("tag", "onclick:" + viewHolder.tv.getText());
                }
            });

            int itemViewMeasuredHeight = viewHolder.itemView.getMeasuredHeight();
            Log.i("tag", "getMeasuredHeight:" + itemViewMeasuredHeight);
            int recyclerViewMeasuredHeight = recyclerView.getMeasuredHeight();
            Log.i("tag", "measuredHeight:" + recyclerViewMeasuredHeight);

            // 需要设置每个item的最小高度为RecyclerView的高度，来实现分页效果
            viewHolder.itemView.setMinimumHeight(recyclerViewMeasuredHeight);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            this.recyclerView = recyclerView;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            TextView pageNumber;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                pageNumber = itemView.findViewById(R.id.pageNumber);
            }
        }

    }
}

