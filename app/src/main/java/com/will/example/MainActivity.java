package com.will.example;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.will.recyclerviewloadingadapter.LoadingAdapter;

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
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.refreshData();
                    }
                },1000);

            }
        });
        adapter.setOnLoadingListener(new LoadingAdapter.OnLoadingListener() {
            @Override
            public void onSuccess() {
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure() {
                refreshLayout.setRefreshing(false);
            }
        });

    }
}
