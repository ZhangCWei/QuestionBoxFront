package com.example.myapplication.view;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.entity.User;
import com.example.myapplication.util.Common;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //数据库连接类
    private EditText Username;
    private EditText PassWord;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    protected void onResume() {
        // 重置用户名和密码输入框
        super.onResume();
        PassWord.setText("");
        Username.setText("");
    }

    @SuppressLint("ObsoleteSdkInt")
    private void initView() {
        // 设置背景图片
        getWindow().setBackgroundDrawableResource(R.drawable.loginbg);

        // 设置状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        // 获取屏幕高度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // 设置 CardView 的高度
        CardView cardView = findViewById(R.id.card_view);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
        params.height = screenHeight / 2;
        params.topMargin = screenHeight / 4;
        cardView.setLayoutParams(params);

        //登录按钮
        Button btnLogin = findViewById(R.id.bt_login);
        //注册按钮
        Button btnRegister = findViewById(R.id.bt_reg);

        Username = findViewById(R.id.et_1);
        PassWord = findViewById(R.id.et_2);

        // 按钮点击事件
        btnLogin.setOnClickListener(this);      // 实际调用 onClick 函数
        btnRegister.setOnClickListener(this::onClickRegister);
    }

    class Threads_Login extends Thread {
        // 在独立的子线程中处理登录操作
        @Override
        public void run() {
            // 获取用户名和密码
            String username = Username.getText().toString();
            String password = Common.MD5(PassWord.getText().toString());

            // 创建请求体
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder().add("phone", username).build();

            // 创建请求
            Request request = new Request.Builder()
                    .url(Common.URL + "/login")
                    .post(body)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            // 使用 OkHttp 库进行异步网络请求
            // 创建一个新的 HTTP 请求调用对象, 将其加入队列, 异步执行请求
            client.newCall(request).enqueue(new Callback() {
                // 请求失败处理
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.log(Level.SEVERE, "Request failed", e);
                    // 确保子线程可安全更新UI
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "请求错误，请稍后再试", Toast.LENGTH_SHORT).show());
                }

                // 请求成功处理
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){                    // 回调方法在子线程执行
                        assert response.body() != null;
                        String userJson= response.body().string();
                        // 反序列化用户信息
                        Common.user = gson.fromJson(userJson, User.class);
                        System.out.println(Common.user);
                        Intent intent;
                        if(Common.user == null){
                            // 用户不存在
                            Looper.prepare();      // 创建消息循环
                            Toast.makeText(MainActivity.this,"该用户不存在！",Toast.LENGTH_SHORT).show();
                            Looper.loop();         // 启动消息循环, 处理消息队列中的消息
                            return;
                        }
                        // 验证密码
                        if (password.equals(Common.user.getRealPassword())) {
                            // 密码正确
                            Looper.prepare();
                            Toast.makeText(MainActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                            intent = new Intent(MainActivity.this, TotalActivity.class);
                            startActivity(intent);
                            System.out.println("log in!");
                            Looper.loop();
                        } else {
                            // 密码错误
                            Looper.prepare();
                            Toast.makeText(MainActivity.this,"账号或密码错误",Toast.LENGTH_SHORT).show();
                            System.out.println("wrong response");
                            Looper.loop();
                        }
                    } else {
                        System.out.println("fail");
                    }
                }
            });
        }
    }

    // 登录按钮点击事件
    public void onClick(View v) {
        // 创建一个新线程对象
        Threads_Login login = new Threads_Login();
        // 启动对象, 执行 run 方法
        login.start();
    }

    // 注册按钮点击事件
    public void onClickRegister(View v){
        // Intent 用于不同组件之间传递信息和启动新组件
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}