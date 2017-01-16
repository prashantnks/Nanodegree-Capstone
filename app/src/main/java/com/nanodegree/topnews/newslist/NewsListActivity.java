package com.nanodegree.topnews.newslist;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.nanodegree.topnews.R;
import com.nanodegree.topnews.databinding.ActivityNewsListBinding;
import com.nanodegree.topnews.drawermenu.DrawerActivity;
import com.nanodegree.topnews.newsdetail.NewsDetailActivity;
import com.nanodegree.topnews.newsdetail.NewsDetailFragment;

public class NewsListActivity extends DrawerActivity {

    private ActivityNewsListBinding binding;
    private NewsListFragment newsListFragment;
    private boolean isTablet = false;
    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_news_list,
                drawerBinding.flContentMain, true);
        initRemoteConfig();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            ViewCompat.setElevation(toolbar, 8);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setTitle(R.string.app_name);
            }
        }

        newsListFragment = (NewsListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_news_list);
        FrameLayout layoutNewsDetail = (FrameLayout) findViewById(R.id.fl_container_news_detail);
        if (layoutNewsDetail != null) {
            isTablet = true;

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fl_container_news_detail, new NewsDetailFragment());
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            isTablet = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerBinding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerBinding.drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    drawerBinding.drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onArticleSelected(int position) {

        if (isTablet) {
            NewsListAdapter adapter = newsListFragment.getAdapter();
            NewsListAdapter.ArticleViewHolder viewHolderLastSelected =
                    (NewsListAdapter.ArticleViewHolder) newsListFragment
                            .getRecyclerView().findViewHolderForLayoutPosition(adapter.selectedIndex);

            if (viewHolderLastSelected != null) {
                viewHolderLastSelected.baseLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            NewsListAdapter.ArticleViewHolder viewHolderSelected =
                    (NewsListAdapter.ArticleViewHolder) newsListFragment
                            .getRecyclerView().findViewHolderForLayoutPosition(adapter.selectedIndex);

            if (viewHolderSelected != null) {
                viewHolderSelected.baseLayout.setBackgroundColor(ContextCompat.getColor(this,
                        R.color.selected_item_bg));
            }

            adapter.selectedIndex = position;

            NewsDetailFragment movieDetailFragment = new NewsDetailFragment();

            Gson gson = new Gson();
            String jsonString = gson.toJson(newsListFragment.getAdapter().getArticleList().get(position));
            getIntent().putExtra("NEWS_DETAIL", jsonString);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fl_container_news_detail, movieDetailFragment);
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            Intent intent = new Intent(this, NewsDetailActivity.class);
            Gson gson = new Gson();
            String jsonString = gson.toJson(newsListFragment.getAdapter().getArticleList().get(position));
            intent.putExtra("NEWS_DETAIL", jsonString);
            startActivity(intent);
        }
    }

    private void initRemoteConfig() {
        remoteConfig = FirebaseRemoteConfig.getInstance();

        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        remoteConfig.setConfigSettings(remoteConfigSettings);
        fetchRemoteConfigValues();
    }

    private void fetchRemoteConfigValues() {
        long cacheExpiration = 3600;

        //expire the cache immediately for development mode.
        if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        remoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // task successful. Activate the fetched data
                            remoteConfig.activateFetched();
                        } else {
                            //task failed
                        }
                    }
                });
    }
}