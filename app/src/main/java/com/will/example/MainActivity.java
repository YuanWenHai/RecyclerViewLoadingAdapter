package com.will.example;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.will.recyclerviewloadingadapter.BaseLoadingAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ExampleAdapter adapter = new ExampleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.useItemAnimation(true);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.load(false, new BaseLoadingAdapter.OnLoadingListener() {
                    @Override
                    public void onResult(boolean which) {
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }
}
