package com.brooks.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brooks.demo.dummy.DummyContent;
import com.brooks.loadmorerecyclerview.LoadMoreRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public class ManualloadActivity extends AppCompatActivity {

    private LoadMoreRecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ItemAdapter adapter;
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manualload);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.manualload);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //加载RecyclerView
        loadRecyclerView();
        //加载swipeRefreshLayout
        loadRefreshLayout();
    }

    private void loadRecyclerView() {
        recyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        adapter = new ItemAdapter(DummyContent.generateData(page));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLoadMoreEnable(DummyContent.hasMore(page));
        recyclerView.setAutoLoadMore(false);
        recyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.addDatas(DummyContent.generateData(++page));
                        recyclerView.notifyMoreFinish(DummyContent.hasMore(page));
                    }
                }, 1000);
            }
        });
        recyclerView.setOnRecyclerViewListener(new LoadMoreRecyclerView.OnRecyclerViewListener() {

            @Override
            public void onItemClick(View v, int position) {
                Log.i("recyclerView", "onItemClick position:" + position);
                Toast.makeText(ManualloadActivity.this, "onItemClick position:" + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                Log.i("recyclerView", "onItemLongClick position:" + position);
                Toast.makeText(ManualloadActivity.this, "onItemLongClick position:" + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void loadRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_color1, R.color.refresh_color2, R.color.refresh_color3, R.color.refresh_color4);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 0;
                adapter = new ItemAdapter(DummyContent.generateData(page));
                recyclerView.setAdapter(adapter);
                recyclerView.setLoadMoreEnable(DummyContent.hasMore(page));
                recyclerView.setAutoLoadMore(false);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
