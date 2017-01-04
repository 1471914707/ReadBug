package com.example.testing.readbug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class ResisgterActivity extends BaseActivity {
    private CheckBox rememberPass;
    private Button btnreg;
    private EditText accountEdit;
    private EditText passwordEdit;
    private boolean reg_suss = false;
    private boolean timeout = false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button btnVerCode;
    private EditText VerCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resisgter);
        btnreg = (Button) findViewById(R.id.reg);
        rememberPass = (CheckBox) findViewById(R.id.read_case);
        accountEdit = (EditText) findViewById(R.id.account_reg);
        passwordEdit = (EditText) findViewById(R.id.password_reg);
        pref = getSharedPreferences("data",MODE_PRIVATE);
        btnVerCode = (Button) findViewById(R.id.btnVerCode);
        VerCode =  ((EditText)findViewById(R.id.code));
        final TimeCountUtil timeCountUtil = new TimeCountUtil(this, 60000, 1000, btnVerCode);
        btnVerCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iphone = accountEdit.getText().toString();
                if (iphone.length() == 11){
                    timeCountUtil.start();
                    Toast.makeText(ResisgterActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResisgterActivity.this, "请输入正确手机号", Toast.LENGTH_SHORT).show();
                }
            }
        });


        rememberPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!rememberPass.isChecked()){
                    btnreg.setClickable(false);
                    btnreg.setText("不可注册");
                }else{
                    btnreg.setClickable(true);
                    btnreg.setText("注册");
                }
            }
        });

        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestWithOkHttp();
                //解决校验线程还没完成下面就被执行的问题
                try{
                    Thread.sleep(1000);
                    timeout = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
                String code = VerCode.getText().toString();
                Boolean emp = passwordEdit.getText().toString().length()<6 && accountEdit.getText().toString().length()<11;
                if (reg_suss && timeout && "1234".equals(code) && emp){
                    editor = pref.edit();
                    editor.putString("iphone",accountEdit.getText().toString());
                    editor.putString("password",passwordEdit.getText().toString());
                    editor.putBoolean("status",true);
                    editor.apply();
                    Intent intent = new Intent(ResisgterActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else if (!timeout){
                    Toast.makeText(ResisgterActivity.this, "注册超时", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(ResisgterActivity.this, "账号已存在", Toast.LENGTH_SHORT).show();
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
                            .add("iphone",accountEdit.getText().toString())
                            .add("password",passwordEdit.getText().toString()).build();
                    Request request = new Request.Builder()
                            .url(getResources().getString(R.string.HOST_URL_1)+"/HandleUserReg")
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
                reg_suss = true;
            }
        }
    }
}
