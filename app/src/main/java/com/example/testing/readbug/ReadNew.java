package com.example.testing.readbug;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import Bean.Article;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReadNew extends BaseActivity {

    private int artId = 0;
    private Article article;
    private TextView title;
    private TextView time;
    private TextView content;
    private TextView author;
    private Button btn_back;


    public static void actionStart(Context context, int id){
        Intent intent = new Intent(context,ReadNew.class);
        intent.putExtra("artId",id);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_new);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar!=null){
            actionbar.hide();
        }
        title = (TextView) findViewById(R.id.New_Title);
        content = (TextView) findViewById(R.id.New_Content);
        author = (TextView) findViewById(R.id.New_author);
        time = (TextView) findViewById(R.id.New_Time);
        btn_back = (Button) findViewById(R.id.New_Back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initArticles();
        //解决校验线程还没完成下面就被执行的问题
        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (article != null){
            title.setText(article.getTitle());
            content.setText(Html.fromHtml(article.getContent()));
            author.setText(article.getName());
            time.setText(article.getTime());
        }

    }

    private void initArticles() {
        Intent intent = getIntent();
        artId = intent.getIntExtra("artId",0);
        sendRequestWithOkHttp();
    }

    private void sendRequestWithOkHttp() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("artId",artId+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(getResources().getString(R.string.HOST_URL_1)+"/GetReadBugArticleContent")
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
            article = list.get(0);
            return ;
        }
    }
}
