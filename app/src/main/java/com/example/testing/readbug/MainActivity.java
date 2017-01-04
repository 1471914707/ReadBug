package com.example.testing.readbug;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import Bean.Article;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,SwipeRefreshLayout.OnRefreshListener {

    private static final int REFRESH_COMPLETE = 0X110;
    private SwipeRefreshLayout mSwipeLayout;
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case REFRESH_COMPLETE:
                    articleListAdd.clear();

                    initArticles();
                    //解决校验线程还没完成下面就被执行的问题
                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    articleList.addAll(0,articleListAdd);
                    adapter.notifyDataSetChanged();
                    mSwipeLayout.setRefreshing(false);
                    updateALastId();
                    break;

            }
        }
    };

    ArticleAdapter adapter = null;
    private List<Article> articleList = new ArrayList<>(30);
    private List<Article> articleListAdd = new ArrayList<>();
    private int ArtType = 0;
    private int lastId=0;

    private FloatingActionButton btn_sport;
    private FloatingActionButton btn_life;
    private FloatingActionButton btn_study;
    private FloatingActionButton btn_index;
    private FloatingActionButton fab;
    private int btn_flag = 0;
    private ListView lst_new;
    private SharedPreferences pref;
    Toolbar toolbar;
    DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lst_new = (ListView) findViewById(R.id.lst_new);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        articleListAdd.clear();
        initArticles();
        //解决校验线程还没完成下面就被执行的问题
        try{
            Thread.sleep(1200);
        }catch (Exception e){
            e.printStackTrace();
        }

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(this);

        articleList.addAll(articleListAdd);
        updateALastId();
        adapter = new ArticleAdapter(MainActivity.this,R.layout.art_item,articleList);
        lst_new.setAdapter(adapter);



       pref = getSharedPreferences("data",MODE_PRIVATE);
        Boolean status = pref.getBoolean("status",false);
        if(status){
            Toast.makeText(this, "欢迎回来", Toast.LENGTH_SHORT).show();
        }


        fab = (FloatingActionButton) findViewById(R.id.fab_main);
        btn_life = (FloatingActionButton) findViewById(R.id.fab_life);
        btn_study = (FloatingActionButton) findViewById(R.id.fab_study);
        btn_sport = (FloatingActionButton) findViewById(R.id.fab_sport);
        btn_index = (FloatingActionButton) findViewById(R.id.fab_index);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btn_flag == 0){
                    ObjectAnimator sport = ObjectAnimator.ofFloat(btn_sport,"translationX",-200f);
                    ObjectAnimator study = ObjectAnimator.ofFloat(btn_study,"translationY",-400f);
                    ObjectAnimator study1 = ObjectAnimator.ofFloat(btn_study,"translationX",-200f);
                    ObjectAnimator life = ObjectAnimator.ofFloat(btn_life,"translationY",-200f);
                    ObjectAnimator life2 = ObjectAnimator.ofFloat(btn_life,"translationX",-200f);
                    ObjectAnimator index1 = ObjectAnimator.ofFloat(btn_index,"translationY",-200f);

                    AnimatorSet animSet = new AnimatorSet();
                    animSet.play(sport).with(study).with(life).with(life2).with(index1).with(study1);
                    animSet.setDuration(300);
                    animSet.start();
                    btn_flag = 1;
                }else{
                    reButton();
                    btn_flag = 0;
                }

            }
        });
        btn_life.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(2,"生活");
            }
        });
        btn_sport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(1,"体育");
            }
        });
        btn_study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(3,"学习");
            }
        });
        btn_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(0,"主页");
            }
        });



        if(status){
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }

        lst_new.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article article = articleList.get(position);
                ReadNew.actionStart(MainActivity.this,article.getId());
            }
        });

    }

    private void startRefresh(int type,String title){
        articleList.clear();
        updateALastId();
        toolbar.setTitle(title);
        ArtType = type;
        mSwipeLayout.setRefreshing(true);
        onRefresh();
        reButton();
        btn_flag = 0;
    }

    private void updateALastId(){
        lastId = 0;
        if(articleList.size()!=0){
            lastId = articleList.get(0).getId();
        }
    }

    private void reButton(){
        ObjectAnimator sport = ObjectAnimator.ofFloat(btn_sport,"translationX",0);
        ObjectAnimator study = ObjectAnimator.ofFloat(btn_study,"translationY",0);
        ObjectAnimator study1 = ObjectAnimator.ofFloat(btn_study,"translationX",0);
        ObjectAnimator life = ObjectAnimator.ofFloat(btn_life,"translationY",0);
        ObjectAnimator life2 = ObjectAnimator.ofFloat(btn_life,"translationX",0);
        ObjectAnimator index2 = ObjectAnimator.ofFloat(btn_index,"translationY",0);

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(sport).with(study).with(life).with(life2).with(index2).with(study1);
        animSet.setDuration(300);
        animSet.start();
    }


    private void initArticles() {
        sendRequestWithOkHttp();
    }

    private void sendRequestWithOkHttp() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("type",ArtType+"").add("artId",lastId+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(getResources().getString(R.string.HOST_URL_1)+"/GetReadBugArticleList")
                            .post(body).build();

                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJSONWithGSON(responseData);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void parseJSONWithGSON(final String responseData) {

        Gson gson = new Gson();
        List<Article> list = gson.fromJson(responseData,new TypeToken<List<Article>>(){}.getType());
        if (list.size() != 0){
            for (Article art:list){
                articleListAdd.add(art);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        pref = getSharedPreferences("data",MODE_PRIVATE);
        Boolean status = pref.getBoolean("status",false);
        if(status){
            getMenuInflater().inflate(R.menu.main, menu);
        }else{
            getMenuInflater().inflate(R.menu.toolbar,menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.choose_login) {
            SharedPreferences.Editor editor;
            editor = pref.edit();
            editor.putBoolean("status",false);
            editor.apply();
            ActivityCollector.finishAll();
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.action_ss){
            //点击搜索
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_exit) {
            //退出所有
            SharedPreferences.Editor editor;
            editor = pref.edit();
            editor.putBoolean("status",false);
            editor.apply();
            ActivityCollector.finishAll();
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_comment){

        }else if(id == R.id.nav_shop){

        }else if(id == R.id.nav_modify){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }
}
