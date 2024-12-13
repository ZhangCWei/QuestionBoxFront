package com.example.myapplication.view;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.entity.User;
import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import okhttp3.*;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener {

    //数据库连接类
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    private EditText Username;
    private EditText PassWord;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        PassWord.setText("");
        Username.setText("");

    }
    private void init()
    {
        getWindow().setBackgroundDrawableResource(R.drawable.loginbg);
        //登录跳转
        Button btnLogin = findViewById(R.id.bt_login);
        //注册跳转
        Button btnRegister = findViewById(R.id.bt_reg);

        Username = findViewById(R.id.et_1);
        PassWord = findViewById(R.id.et_2);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this::onClickRegister);
    }


    class Threads_Login extends Thread {
        //private Socket client = null;
        private BufferedReader in;
        private BufferedWriter out;

        @Override
        public void run() {
            String username = Username.getText().toString();
            String password = PassWord.getText().toString();  //用户发送的信息
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("phone",username)
                    .build();
            Request request = new Request.Builder()
                    .url(Common.URL+"/login")
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){    // 回调的方法执行在子线程。
                        String userJson= response.body().string();
                        //Type type = new TypeToken<List<User>>() {
                        //}.getType(); //泛型类型，import com.google.gson.reflect.TypeToken;
                        Common.user = gson.fromJson(userJson, User.class);//反序列化
                        Intent intent = null;
                        if(Common.user == null){
                            Looper.prepare();
                            Toast.makeText(MainActivity.this,"该用户不存在！",Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            return;
                        }
                        if (password.equals(Common.user.getRealpassword())) {
                            Looper.prepare();
                            Toast.makeText(MainActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                            intent = new Intent(MainActivity.this, TotalActivity.class);
                            startActivity(intent);
                            System.out.println("log in!");
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(MainActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                            System.out.println("wrong response");
                            Looper.loop();
                        }
                    }
                    else {
                        System.out.println("fail");
                    }
                }
            });

        }
    }
        public void onClick(View v) {
            Threads_Login login = new Threads_Login();
            login.start();
        }
        public void onClickRegister(View v){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
}