package com.example.testing.readbug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import Bean.Users;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private EditText accountEdit;
    private EditText passwordEdit;
    private Button login;
    private String iphone = null;
    private boolean login_suss = false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref = getSharedPreferences("data",MODE_PRIVATE);
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);

        TextView reg = (TextView) findViewById(R.id.go_regsister);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,ResisgterActivity.class);
                startActivity(intent);
            }
        });

        boolean isRemember = pref.getBoolean("remember_password",false);
        if(isRemember){
            accountEdit.setText(pref.getString("account",""));
            passwordEdit.setText(pref.getString("password",""));
            rememberPass.setChecked(true);
        }

        Button skipLogin = (Button)findViewById(R.id.skip_login);
        skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                sendRequestWithOkHttp();
                //解决校验线程还没完成下面就被执行的问题
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (account.length()!=0 && password.length()!=0 && login_suss){
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    editor = pref.edit();
                    if (rememberPass.isChecked()){
                        editor.putBoolean("remember_password",true);
                        editor.putString("iphone",iphone);
                        editor.putString("account",account);
                        editor.putString("password",password);
                        editor.putBoolean("status",true);
                    }else{
                        editor.clear();
                        editor.putString("iphone",iphone);
                        editor.putString("account",account);
                        editor.putString("password",password);
                        editor.putBoolean("status",true);
                    }
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "密码或账号错误或连接超时", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendRequestWithOkHttp() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                        RequestBody body = new FormBody.Builder()
                                .add("account",accountEdit.getText().toString())
                                .add("password",passwordEdit.getText().toString()).build();
                        Request request = new Request.Builder()
                                .url(getResources().getString(R.string.HOST_URL_1)+"/HandleUserLogin")
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
        List<Users> userList = gson.fromJson(responseData,new TypeToken<List<Users>>(){}.getType());
        for (Users u:userList){
            if (u != null){
                editor = pref.edit();
                editor.putInt("id",u.getId());
                editor.apply();
                login_suss = true;
            }
        }
    }
}
